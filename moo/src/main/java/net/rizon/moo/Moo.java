package net.rizon.moo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import net.rizon.moo.io.ClientInitializer;
import net.rizon.moo.io.IRCMessage;
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
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import net.rizon.moo.conf.Config;
import net.rizon.moo.events.InitDatabases;
import net.rizon.moo.events.LoadDatabases;
import net.rizon.moo.events.OnShutdown;
import net.rizon.moo.events.SaveDatabases;
import net.rizon.moo.protocol.ProtocolPlugin;
import net.rizon.moo.protocol.plexus;

class DatabaseTimer implements Runnable
{
	@Override
	public void run()
	{
		Moo.getEventBus().post(new SaveDatabases());
	}
}

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
	private static Date created = new Date();
	
	private EventLoopGroup group = new NioEventLoopGroup(1);
	private io.netty.channel.Channel channel;
	
	public static EventBus eventBus = new EventBus();

	public static Config conf = null;
	public static Database db = null;
	public static ChannelManager channels = null;
	public static UserManager users = null;
	public static boolean quitting = false;
	public static ProtocolPlugin protocol = null;

	public static String akillServ = "GeoServ";

	public static User me = null;
	public static Moo moo;
	
	public static Injector injector;
	
	public static void main(String[] args)
	{
		moo = new Moo();
		moo.start();
	}
	
	Moo()
	{
	}
	
	public void handshake()
	{
		if (conf.general.server_pass != null)
			write("PASS", conf.general.server_pass);

		write("USER", conf.general.ident, ".", ".", conf.general.realname);
		write("NICK", conf.general.nick);

		write("PROTOCTL", "UHNAMES NAMESX");
	}
	
	private void run() throws InterruptedException
	{
		try
		{
			Bootstrap client = new Bootstrap()
			    .group(group)
			    .channel(NioSocketChannel.class)
			    .handler(new ClientInitializer(this))
			    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30 * 1000);
		    
			channels = new ChannelManager();
			users = new UserManager();

			if (conf.general.host != null)
				client.bind(new InetSocketAddress(conf.general.host, 0)).sync().await();

			ChannelFuture future = client.connect(conf.general.server, conf.general.port);
			channel = future.channel();

			channel.closeFuture().sync();
		}
		finally
		{
		    channels = null;
		    users = null;
		}
	}

	public void start()
	{
		Version.load();

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

		Server.init();

		try
		{
			Moo.protocol = new plexus();//(ProtocolPlugin) Plugin.loadPluginCore("net.rizon.moo.protocol.", Moo.conf.general.protocol.getName());
		}
		catch (Throwable ex)
		{
			logger.error("Error loading protocol", ex);
			System.exit(-1);
		}

		for (String pkg : conf.plugins)
		{
			logger.debug("Loading plugin: {}", pkg);
			try
			{
				Plugin.loadPlugin(pkg);
			}
			catch (Throwable ex)
			{
				logger.error("Error loading plugin " + pkg, ex);
				System.exit(-1);
			}
		}
		
		List<Module> modules = new ArrayList<>();
		modules.add(protocol);
		for (Plugin p : Plugin.getPlugins())
			modules.add(p);
		modules.add(new MooModule());
		injector = Guice.createInjector(modules);
		
		CommandManager cm = injector.getInstance(CommandManager.class);
//		injector.injectMembers(this);

		logger.info("moo v{} starting up", Version.getFullVersion());

		eventBus.post(new InitDatabases());

		eventBus.post(new LoadDatabases());
		
		scheduleWithFixedDelay(new DatabaseTimer(), 5, TimeUnit.MINUTES);

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
	
	public static EventBus getEventBus()
	{
		return eventBus;
	}

	public static void write(String command, Object... args)
	{
		if (moo.channel == null)
			return;
		
		String[] params = new String[args.length];
		int i = 0;
		for (Object o : args)
			params[i++] = o.toString();
		
		IRCMessage message = new IRCMessage(null, command, params);
		moo.channel.writeAndFlush(message);
	}

	public static void privmsg(String target, final String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		write("PRIVMSG", target, buffer);
	}

	/**
	 * Sends the same message to all targets.
	 * @param targets Array of targets.
	 * @param buffer Message to send.
	 */
	public static void privmsgAll(final String[] targets, final String buffer)
	{
		for (String s : targets)
			privmsg(s, buffer);
	}

	public static void notice(String target, final String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		write("NOTICE", target, buffer);
	}

	public static void reply(String source, String target, final String buffer)
	{
		if (target.equalsIgnoreCase(Moo.conf.general.nick))
			notice(source, buffer);
		else
			privmsg(target, buffer);
	}

	public static void join(String target)
	{
		write("JOIN", target);
	}

	public static void kick(final String target, final String channel, final String reason)
	{
		write("KICK", channel, target, reason);
	}

	public static void mode(final String target, final String modes)
	{
		String str = target + " " + modes;
		write("MODE", (Object[]) str.split(" "));
	}

	public static void kill(final String nick, final String reason)
	{
		write("KILL", nick, reason);
	}

	public static void akill(final String host, final String time, final String reason)
	{
		if (host.equals("255.255.255.255"))
			return;

		privmsg(akillServ, "AKILL ADD " + time + " *@" + host + " " + reason);
	}

	public static void qakill(final String nick, final String reason)
	{
		privmsg(akillServ, "QAKILL " + nick + " " + reason);
	}

	public static void capture(final String nick)
	{
		privmsg("RootServ", "CAPTURE " + nick);
	}

	public static void operwall(final String message)
	{
		write("OPERWALL", message);
	}
}
