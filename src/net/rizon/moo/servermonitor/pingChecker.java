package net.rizon.moo.servermonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.logger;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class pingChecker extends Thread
{
	private static final Pattern packetLossPattern = Pattern.compile("([0-9])*% packet loss");
	// server name => error
	private static final HashMap<String, String> errlist = new HashMap<String, String>();
	
	private String s;
	private int len;
	
	public pingChecker(server s, int len)
	{
		this.s = s.getName();
		this.len = len;
	}
	
	@Override
	public void run()
	{
		Process proc = null;
		BufferedReader is = null, es = null;
		
		try
		{
			proc = Runtime.getRuntime().exec(new String[] { "ping", "-c", String.valueOf(this.len), this.s });
			is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			es = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
			
			for (String i; (i = is.readLine()) != null;)
			{
				Matcher m = packetLossPattern.matcher(i);
				if (m.find())
				{
					int loss = Integer.parseInt(m.group(1));
					if (loss != 0)
						for (final String chan : moo.conf.getAdminChannels())
							moo.privmsg(chan, "[" + s + "] Packet loss: " + i);
				}
			}
			
			// All ping error messages are one-liners, woot.
			String err = es.readLine();
			if (err != null)
			{
				if (errlist.containsKey(s) && errlist.get(s).equals(err))
					return;
				
				for (final String chan : moo.conf.getAdminChannels())
					moo.privmsg(chan, "[" + s + "] " + err);
				
				errlist.put(s, err);
			}
			else
				errlist.remove(s);
		}
		catch (IOException ex)
		{
			logger.getGlobalLogger().log(ex);
		}
		finally
		{
			try { proc.getOutputStream().close(); }
			catch (Exception ex) { }
			try { is.close(); }
			catch (Exception ex) { }
			try { es.close(); }
			catch (Exception ex) { }
			try { proc.destroy(); }
			catch (Exception ex) { }
		}
	}
}