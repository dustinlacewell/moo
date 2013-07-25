package net.rizon.moo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.sql.SQLException;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


class databaseTimer extends timer
{
	public databaseTimer()
	{
		super(600, true);
		this.start();
	}

	@Override
	public void run(Date now)
	{
		for (event e : event.getEvents())
			e.saveDatabases();
	}
}

public class moo
{
	private static Date created = new Date();

	public static config conf = null;
	public static socket sock = null;
	public static database db = null;
	public static boolean quitting = false;

	public static void main(String[] args)
	{
		version.load();
		
		try
		{
			conf = new config();
			conf.load();
		}
		catch (Exception ex)
		{
			System.out.println("Error loading configuration");
			ex.printStackTrace();
			System.exit(-1);
		}
		
		try
		{
			if (moo.conf.getDatabase().isEmpty() == false)
				db = new database();
		}
		catch (ClassNotFoundException ex)
		{
			System.out.println("Error loading database driver");
			ex.printStackTrace();
			System.exit(-1);
		}
		catch (SQLException ex)
		{
			System.out.println("Error initializing database");
			ex.printStackTrace();
			System.exit(-1);
		}
		
		try
		{
			for (final String db_class : conf.getDatabaseClasses())
				Class.forName(db_class);
			
			Class<?> c = Class.forName("net.rizon.moo.protocol." + moo.conf.getProtocol() + "." + moo.conf.getProtocol());
			Constructor<?>[] cons = c.getConstructors();
			cons[0].newInstance();
			
			for (final String pkg : conf.getPackages())
			{
				c = Class.forName("net.rizon.moo." + pkg + "." + pkg);
				cons = c.getConstructors();
				cons[0].newInstance();
			}
		}
		catch (Exception ex)
		{
			System.out.println("Error loading resources");
			ex.printStackTrace();
			System.exit(-1);
		}

		System.out.println("Starting up " + conf.getNick());

		for (event e : event.getEvents())
			e.initDatabases();
		
		new databaseTimer();
		
		while (quitting == false)
		{
			try
			{
				if (moo.conf.getSSL())
					sock = socket.createSSL();
				else
					sock = socket.create();
				
				if (conf.getHost().isEmpty() == false)
					sock.getSocket().bind(new InetSocketAddress(conf.getHost(), 0));
				
				sock.connect(conf.getServer(), conf.getPort());
				
				if (conf.getServerPass().isEmpty() == false)
					sock.write("PASS :" + conf.getServerPass());
				
				sock.write("USER " + conf.getIdent() + " . . :" + conf.getRealname());
				sock.write("NICK :" + conf.getNick());
				
				long last_timer_check = System.currentTimeMillis() / 1000L;

				for (String in; (in = sock.read()) != null;)
				{
					try
					{
						long now = System.currentTimeMillis() / 1000L;
						if (now - last_timer_check >= 5)
						{
							timer.processTimers();
							last_timer_check = now;
						}
					}
					catch (Exception ex)
					{
						System.out.println("Error processing timers");
						ex.printStackTrace();
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
						
						if (moo.conf.getDebug() > 2)
						{
							System.out.println("  Source: " + source);
							System.out.println("  Message: " + message_name);
							for (int i = 0; i < buffer.length; ++i)
								System.out.println("    " + i + ": " + buffer[i]);
						}

						message.runMessage(source, message_name, buffer);
					}
					catch (Exception ex)
					{
						System.out.println("Error running message: " + in);
						ex.printStackTrace();
					}
				}
			}
			catch (IOException ex)
			{
				ex.printStackTrace();
			}
			
			for (event e : event.getEvents())
				e.onShutdown();
			
			if (moo.sock != null)
			{
				moo.sock.shutdown();
				moo.sock = null;
			}
			
			for (event e : event.getEvents())
				e.saveDatabases();
			
			try
			{
				Thread.sleep(10 * 1000);
			}
			catch (InterruptedException e)
			{
				quitting = true;
			}
		}
		
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
		moo.sock.write("PRIVMSG " + target + " :" + buffer);
	}
	
	public static void notice(String target, final String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		moo.sock.write("NOTICE " + target + " :" + buffer);
	}
	
	public static void reply(String source, String target, final String buffer)
	{
		if (target.equalsIgnoreCase(moo.conf.getNick()))
			notice(source, buffer);
		else
			privmsg(target, buffer);
	}
	
	public static void join(String target)
	{
		moo.sock.write("JOIN " + target);
	}
	
	public static void kick(final String target, final String channel, final String reason)
	{
		moo.sock.write("KICK " + channel + " " + target + " :" + reason);
	}
	
	public static void mode(final String target, final String modes)
	{
		moo.sock.write("MODE " + target + " " + modes);
	}
	
	public static void kill(final String nick, final String reason)
	{
		moo.sock.write("KILL " + nick + " :" + reason);
	}
	
	public static void akill(final String host, final String time, final String reason)
	{
		if (host.equals("255.255.255.255"))
			return;
		privmsg("GeoServ", "AKILL ADD " + time + " *@" + host + " " + reason);
	}
	
	public static void qakill(final String nick, final String reason)
	{
		privmsg("GeoServ", "QAKILL " + nick + " " + reason);
	}
	
	public static void capture(final String nick)
	{
		privmsg("RootServ", "CAPTURE " + nick);
	}
	
	public static void operwall(final String message)
	{
		moo.sock.write("WALLOPS :" + message);
	}
}
