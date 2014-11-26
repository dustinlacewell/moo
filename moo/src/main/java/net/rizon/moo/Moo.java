package net.rizon.moo;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.conf.Config;
import net.rizon.moo.protocol.ProtocolPlugin;

class databaseTimer extends Timer
{
	public databaseTimer()
	{
		super(600, true);
		this.start();
	}

	@Override
	public void run(Date now)
	{
		for (Event e : Event.getEvents())
			e.saveDatabases();
	}
}

public class Moo
{
	private static final Logger log = Logger.getLogger(Moo.class.getName());
	private static Date created = new Date();

	public static Config conf = null;
	public static Socket sock = null;
	public static Database db = null;
	public static ChannelManager channels = null;
	public static UserManager users = null;
	public static boolean quitting = false;
	public static ProtocolPlugin protocol = null;

	public static String akillServ = "GeoServ";

	public static User me = null;

	public static void main(String[] args)
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
			log.log(Level.INFO, "Loading plugin: {0}", pkg);
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

		log.log(Level.INFO, "moo v{0} starting up", Version.getFullVersion());

		for (Event e : Event.getEvents())
			e.initDatabases();

		for (Event e : Event.getEvents())
			e.loadDatabases();

		new databaseTimer();

		while (quitting == false)
		{
			channels = new ChannelManager();
			users = new UserManager();

			try
			{
				if (conf.general.ssl)
					sock = Socket.createSSL();
				else
					sock = Socket.create();

				if (conf.general.host != null)
					sock.getSocket().bind(new InetSocketAddress(conf.general.host, 0));

				sock.connect(conf.general.server, conf.general.port);

				if (conf.general.server_pass != null)
					sock.write("PASS :" + conf.general.server_pass);

				sock.write("USER " + conf.general.ident + " . . :" + conf.general.realname);
				sock.write("NICK :" + conf.general.nick);

				sock.write("PROTOCTL UHNAMES NAMESX");

				long last_timer_check = System.currentTimeMillis() / 1000L;

				for (String in; (in = sock.read()) != null;)
				{
					try
					{
						long now = System.currentTimeMillis() / 1000L;
						if (now - last_timer_check >= 5)
						{
							Timer.processTimers();
							last_timer_check = now;
						}
					}
					catch (Exception ex)
					{
						log.log(Level.SEVERE, "Error processing timers", ex);
					}

					try
					{
						String[] tokens = in.split(" ");
						if (tokens.length < 2)
							continue;

						String source = null;
						int begin = 0;
						if (tokens[begin].startsWith(":"))
							source = tokens[begin++].substring(1);

						String message_name = tokens[begin++];

						int end = begin;
						for (; end < tokens.length; ++end)
							if (tokens[end].startsWith(":"))
								break;
						if (end == tokens.length)
							--end;

						String[] buffer = new String[end - begin + 1];
						int buffer_count = 0;

						for (int i = begin; i < end; ++i)
							buffer[buffer_count++] = tokens[i];

						if (buffer.length > 0)
							buffer[buffer_count] = tokens[end].startsWith(":") ? tokens[end].substring(1) : tokens[end];
						for (int i = end + 1; i < tokens.length; ++i)
							buffer[buffer_count] += " " + tokens[i];

						/*if (Moo.conf.getDebug() > 2)
						{
							log.log(Level.FINEST, "  Source: " + source);
							log.log(Level.FINEST, "  Message: " + message_name);
							for (int i = 0; i < buffer.length; ++i)
								log.log(Level.FINEST, "    " + i + ": " + buffer[i]);
						}*/

						Message.runMessage(source, message_name, buffer);
					}
					catch (Exception ex)
					{
						log.log(Level.WARNING, "Error running message: " + in, ex);
					}
				}
			}
			catch (IOException ex)
			{
				Logger.getGlobalLogger().log(ex);
			}

			if (Moo.sock != null)
			{
				Moo.sock.shutdown();
				Moo.sock = null;
			}

			for (Event e : Event.getEvents())
				e.saveDatabases();

			channels = null;
			users = null;
			me = null;

			try
			{
				Thread.sleep(10 * 1000);
			}
			catch (InterruptedException e)
			{
				quitting = true;
			}
		}

		for (Event e : Event.getEvents())
			e.onShutdown();

		db.shutdown();

		System.exit(0);
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

	public static void privmsg(String target, final String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		Moo.sock.write("PRIVMSG " + target + " :" + buffer);
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
		Moo.sock.write("NOTICE " + target + " :" + buffer);
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
		Moo.sock.write("JOIN " + target);
	}

	public static void kick(final String target, final String channel, final String reason)
	{
		Moo.sock.write("KICK " + channel + " " + target + " :" + reason);
	}

	public static void mode(final String target, final String modes)
	{
		Moo.sock.write("MODE " + target + " " + modes);
	}

	public static void kill(final String nick, final String reason)
	{
		Moo.sock.write("KILL " + nick + " :" + reason);
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
		Moo.sock.write("OPERWALL :" + message);
	}
}
