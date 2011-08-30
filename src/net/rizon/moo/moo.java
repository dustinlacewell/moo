package net.rizon.moo;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Enumeration;

public class moo
{
	public static config conf = null;
	public static socket sock = null;
	public static boolean quitting = false;
	
	private static void loadClass(final String path) throws Exception
	{
		ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		Enumeration<URL> resources = classLoader.getResources(path.replace('.', '/'));
			
		while (resources.hasMoreElements())
		{
			URL resource = resources.nextElement();
			File dir = new File(resource.getFile());
			
			if (dir.isDirectory() == false)
				continue;

			for (File file : dir.listFiles())
			{
				if (file.getName().endsWith(".class"))
				{
					Class<?> c = classLoader.loadClass(path + "." + file.getName().substring(0, file.getName().length() - 6));
					Constructor<?>[] cons = c.getConstructors();
					if (cons.length > 0)
					{
						try
						{
							cons[0].newInstance();
							if (conf.getDebug() > 3)
								System.out.println("Loading class " + c.getName());
						}
						catch (IllegalAccessException ex)
						{
							if (conf.getDebug() > 3)
								System.out.println("Unable to load " + c.getName() + ", Illegal access exception");
						}
					}
				}
			}
		}
	}

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
			loadClass("net.rizon.moo.messages");
			loadClass("net.rizon.moo.commands");
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
				sock = new socket();
				
				if (conf.getHost() != null && conf.getHost().isEmpty() == false)
					sock.bind(new InetSocketAddress(conf.getHost(), 0));
				
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
			
			try
			{
				Thread.sleep(10 * 1000);
			}
			catch (InterruptedException e)
			{
				quitting = true;
			}
		}
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
}
