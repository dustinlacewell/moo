package net.rizon.moo;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.ScheduledFuture;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static net.rizon.moo.Moo.conf;

import net.rizon.moo.conf.Config;
import net.rizon.moo.protocol.ProtocolPlugin;

class DatabaseTimer implements Runnable
{
	@Override
	public void run()
	{
		for (Event e : Event.getEvents())
			e.saveDatabases();
	}
}

class RunnableContainer implements Runnable
{
	private static final Logger log = Logger.getLogger(RunnableContainer.class.getName());
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
			log.log(Level.WARNING, "Erorr while running scheduled event", ex);
		}
	}
}

public class Moo
{
	private static final Logger log = Logger.getLogger(Moo.class.getName());
	private static Date created = new Date();
	
	private EventLoopGroup group = new NioEventLoopGroup(1);
	private io.netty.channel.Channel channel;

	public static Config conf = null;
	public static Database db = null;
	public static ChannelManager channels = null;
	public static UserManager users = null;
	public static boolean quitting = false;
	public static ProtocolPlugin protocol = null;

	public static String akillServ = "GeoServ";

	public static User me = null;
	public static Moo moo;
	
	public static void main(String[] args)
	{
		moo = new Moo();
		moo.start();
	}
	
	Moo()
	{
	}
	
	protected void handshake()
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
			log.log(Level.SEVERE, "Error loading configuration", ex);
			System.exit(-1);
		}

		try
		{
			if (conf.database != null)
				db = new Database(conf.database);
		}
		catch (ClassNotFoundException ex)
		{
			log.log(Level.SEVERE, "Error loading database driver", ex);
			System.exit(-1);
		}
		catch (SQLException ex)
		{
			log.log(Level.SEVERE, "Error initializing database", ex);
			System.exit(-1);
		}

		Server.init();

		try
		{
			Moo.protocol = (ProtocolPlugin) Plugin.loadPluginCore("net.rizon.moo.protocol.", Moo.conf.general.protocol.getName());
		}
		catch (Throwable ex)
		{
			log.log(Level.SEVERE, "Error loading protocol", ex);
			System.exit(-1);
		}

		for (String pkg : conf.plugins)
		{
			log.log(Level.INFO, "Loading plugin: " + pkg);
			try
			{
				Plugin.loadPlugin(pkg);
			}
			catch (Throwable ex)
			{
				log.log(Level.SEVERE, "Error loading plugin " + pkg, ex);
				System.exit(-1);
			}
		}

		log.log(Level.INFO, "moo v" + Version.getFullVersion() + " starting up");

		for (Event e : Event.getEvents())
			e.initDatabases();

		for (Event e : Event.getEvents())
			e.loadDatabases();
		
		group.scheduleWithFixedDelay(new DatabaseTimer(), 1, 1, TimeUnit.MINUTES);

		while (quitting == false)
		{
			try
			{
				run();
			}
			catch (Exception ex)
			{
				log.log(Level.SEVERE, "Error thrown out of run()", ex);
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
		
		for (Event e : Event.getEvents())
			e.saveDatabases();

		for (Event e : Event.getEvents())
			e.onShutdown();

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

	public static boolean matches(String text, String pattern)
	{
		text = text.toLowerCase();
		pattern = pattern.toLowerCase();

		pattern = pattern.replaceAll("\\.", "\\\\.");
		pattern = pattern.replaceAll("\\*", "\\.\\*");
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(text);
		return m.matches();
	}

	public static boolean wmatch(String matchtxt, String txt)
	{
		String mt = "";

		for (int x = 0; x < matchtxt.length(); x++)
			switch (matchtxt.charAt(x))
			{
				case '*':
					mt += ".*";
					break;
				case '?':
					mt += ".";
					break;
				case '^':
				case '$':
				case '(':
				case ')':
				case '[':
				case ']':
				case '.':
				case '|':
				case '+':
				case '{':
				case '}':
				case '\\':
					mt += "\\" + matchtxt.charAt(x);
					break;
				default:
					mt += matchtxt.substring(x, x + 1);
			}

		Pattern p = Pattern.compile("(?uis)^" + mt + "$");
		Matcher m = p.matcher(txt);
		return m.find() == true;
	}

	public static String difference(Date now, Date then)
	{
		long lnow = now.getTime() / 1000L, lthen = then.getTime() / 1000L;

		long ldiff = now.compareTo(then) > 0 ? lnow - lthen : lthen - lnow;
		int days = 0, hours = 0, minutes = 0;

		if (ldiff == 0)
			return "0 seconds";

		while (ldiff > 86400)
		{
			++days;
			ldiff -= 86400;
		}
		while (ldiff > 3600)
		{
			++hours;
			ldiff -= 3600;
		}
		while (ldiff > 60)
		{
			++minutes;
			ldiff -= 60;
		}

		String buffer = "";
		if (days > 0)
			buffer += days + " day" + (days == 1 ? "" : "s") + " ";
		if (hours > 0)
			buffer += hours + " hour" + (hours == 1 ? "" : "s") + " ";
		if (minutes > 0)
			buffer += minutes + " minute" + (minutes == 1 ? "" : "s") + " ";
		if (ldiff > 0)
			buffer += ldiff + " second" + (ldiff == 1 ? "" : "s") + " ";
		buffer = buffer.trim();

		return buffer;
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
		write("MODE", target, modes);
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
