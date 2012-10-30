package net.rizon.moo.servermonitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.moo;
import net.rizon.moo.server;

public class pingChecker extends Thread
{
	private static final Pattern packetLossPattern = Pattern.compile("([0-9])*% packet loss");
	
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
		BufferedReader is = null;
		
		try
		{
			Process proc = Runtime.getRuntime().exec(new String[] { "ping", "-c", String.valueOf(this.len), this.s });
			is = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
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
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try { is.close(); }
			catch (Exception ex) { }
		}
	}
}