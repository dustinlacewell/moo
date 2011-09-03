package net.rizon.moo;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;

public class moo
{
	public static config conf = null;
	public static socket sock = null;
	public static boolean quitting = false;
	
	private static final String[] messages = { "message001", "message015", "message213", "message364", "message474", "messageInvite", "messageNotice", "messagePing", "messagePrivmsg" };
	private static final String[] commands = { "commandFlood", "commandHelp", "commandMap", "commandScheck", "commandShell", "commandShutdown", "commandSid", "commandSplit", "commandStatus", "commandVersions" };

	public static void main(String[] args)
	{
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
			for (int i = 0; i < messages.length; ++i)
			{
				Class<?> c = Class.forName("net.rizon.moo.messages." + messages[i]);
				Constructor<?>[] cons = c.getConstructors();
				cons[0].newInstance();
			}
			
			for (int i = 0; i < commands.length; ++i)
			{
				Class<?> c = Class.forName("net.rizon.moo.commands." + commands[i]);
				Constructor<?>[] cons = c.getConstructors();
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
		
		while (quitting == false)
		{
			try
			{
				if (moo.conf.getSSL())
					sock = socket.createSSL();
				else
					sock = socket.create();
				
				if (conf.getHost() != null && conf.getHost().isEmpty() == false)
					sock.getSocket().bind(new InetSocketAddress(conf.getHost(), 0));
				
				sock.connect(conf.getServer(), conf.getPort());
				
				if (conf.getServerPass() != null && conf.getServerPass().isEmpty() == false)
					sock.write("PASS :" + conf.getServerPass());
				
				sock.write("USER " + conf.getIdent() + " . . :" + conf.getRealname());
				sock.write("NICK :" + conf.getNick());

				for (String in; (in = sock.read()) != null;)
				{
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
						
						buffer[buffer_count] = tokens[end].substring(1);
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
			
			if (moo.sock != null)
			{
				moo.sock.shutdown();
				moo.sock = null;
			}
			
			try
			{
				Thread.sleep(10 * 1000);
			}
			catch (InterruptedException e)
			{
				quitting = true;
			}
		}
		
		System.exit(0);
	}
	
	public static boolean match(String text, String pattern)
	{
		String[] tokens = pattern.split("\\*");
		
		for (String token : tokens)
		{
			int idx = text.indexOf(token);

			if (idx == -1)
				return false;

			text = text.substring(idx + token.length());
		}
		
		return true;
	}
	
	public static void akill(final String host, final String time, final String reason)
	{
		if (host.equals("255.255.255.255"))
			return;
		sock.privmsg("GeoServ", "AKILL ADD " + time + " *@" + host + " " + reason);
	}
}
