package net.rizon.moo;

import com.google.common.eventbus.EventBus;
import net.rizon.moo.irc.User;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import net.rizon.moo.io.ClientInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import net.rizon.moo.conf.ConfPlugin;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventManager;
import net.rizon.moo.events.InitDatabases;
import net.rizon.moo.events.LoadDatabases;
import net.rizon.moo.events.OnShutdown;
import net.rizon.moo.events.SaveDatabases;
import net.rizon.moo.io.NettyModule;
import net.rizon.moo.protocol.Plexus;

class RunnableContainer implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(RunnableContainer.class);
	private final Runnable runnable;

	public RunnableContainer(Runnable r)
	{
		this.runnable = r;
	}

	@Override
	public void run()
	{
		try
		{
			this.runnable.run();
		}
		catch (Exception ex)
		{
			logger.warn("Error while running scheduled event", ex);
		}
	}
}

public class Moo
{
	private static final Logger logger = LoggerFactory.getLogger(Moo.class);
	private static final Date created = new Date();
	
	private final EventLoopGroup group = new NioEventLoopGroup(1);
	public static io.netty.channel.Channel channel;

	private Config conf;
	public static Database db = null;
	public static boolean quitting = false;

	public static String akillServ = "GeoServ";

	public static User me = null;
	public static Moo moo;
	
	public static Injector injector;

	@Inject
	private EventBus eventBus;

	@Inject
	private DatabaseTimer databaseTimer;

	@Inject
	private EventManager eventManager;

	@Inject
	private PluginManager pluginManager;
	
	public static void main(String[] args)
	{
		moo = new Moo();
		moo.start();
	}

	public Config getConf()
	{
		return conf;
	}

	public void setConf(Config conf)
	{
		this.conf = conf;
	}
	
	private void run() throws InterruptedException
	{
		Bootstrap client = new Bootstrap()
		    .group(group)
		    .channel(NioSocketChannel.class)
		    .handler(injector.getInstance(ClientInitializer.class))
		    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000);
		
		if (conf.general.host != null)
			client.bind(new InetSocketAddress(conf.general.host, 0)).sync().await();

		ChannelFuture future = client.connect(conf.general.server, conf.general.port);
		channel = future.channel();

		channel.closeFuture().sync();
	}

	public void start()
	{
		try
		{
			conf = Config.load();
		}
		catch (Exception ex)
		{
			logger.error("Error loading configuration", ex);
			System.exit(-1);
		}

		try
		{
			if (conf.database != null)
				db = new Database(conf.database);
		}
		catch (ClassNotFoundException ex)
		{
			logger.error("Error loading database driver", ex);
			System.exit(-1);
		}
		catch (SQLException ex)
		{
			logger.error("Error initializing database", ex);
			System.exit(-1);
		}

		buildInjector();

		for (ConfPlugin plugin : conf.plugins)
		{
			logger.debug("Loading plugin: {}/{}/{}", plugin.groupId, plugin.artifactId, plugin.version);

			try
			{
				pluginManager.loadPlugin(plugin.groupId, plugin.artifactId, plugin.version);
			}
			catch (Throwable ex)
			{
				logger.error("Error loading plugin", ex);
				//logger.error("Error loading plugin " + pkg, ex);
				System.exit(-1);
			}
		}

		logger.info("moo revision {} starting up", Version.GIT_REVISION);

		buildInjector();

		eventManager.build();

		eventBus.post(new InitDatabases());

		eventBus.post(new LoadDatabases());

		scheduleWithFixedDelay(databaseTimer, 5, TimeUnit.MINUTES);

		while (quitting == false)
		{
			try
			{
				run();
			}
			catch (Exception ex)
			{
				logger.error("Error thrown out of run()", ex);
			}
			
			try
			{
				Thread.sleep(60 * 1000);
			}
			catch (InterruptedException e)
			{
				quitting = true;
			}
		}
		
		eventBus.post(new SaveDatabases());

		eventBus.post(new OnShutdown());

		db.shutdown();

		System.exit(0);
	}
	
	public EventLoopGroup getGroup()
	{
		return group;
	}
	
	public static void stop()
	{
		moo.channel.close();
	}

	public static final String getCreated()
	{
		return created.toString();
	}
	
	public static ScheduledFuture scheduleWithFixedDelay(Runnable r, long t, TimeUnit unit)
	{
		return moo.group.scheduleWithFixedDelay(new RunnableContainer(r), t, t, unit);
	}
	
	public static ScheduledFuture scheduleAtFixedRate(Runnable r, long t, TimeUnit unit)
	{
		return moo.group.scheduleAtFixedRate(new RunnableContainer(r), t, t, unit);
	}
	
	public static ScheduledFuture schedule(Runnable r, long t, TimeUnit unit)
	{
		return moo.group.schedule(r, t, unit);
	}

	private void buildInjector()
	{
		List<Module> modules = new ArrayList<>();

		modules.add(new MooModule(this));
		modules.add(new NettyModule());
		modules.add(new Plexus());

		if (pluginManager != null)
			for (Plugin p : pluginManager.getPlugins())
				modules.add(p);

		injector = Guice.createInjector(Stage.PRODUCTION, modules);

		injector.injectMembers(this);

		eventManager.build();

		for (Plugin p : pluginManager.getPlugins())
			try
			{
				p.start();
			}
			catch (Exception ex)
			{
				logger.warn("unable to start plugin " + p.getName(), ex);
			}
	}

	public void rebuildInjector()
	{
		for (Plugin p : pluginManager.getPlugins())
			p.stop();

		buildInjector();
	}
}
