package net.rizon.moo.commands;

import net.rizon.moo.command;
import net.rizon.moo.moo;

public class commandStatus extends command
{
	public commandStatus()
	{
		super("!STATUS");
	}
	
	private String convertBytes(long bb)
	{
		String what = "bytes";
		
		if (bb > 1024L)
		{
			bb /= 1024L;
			what = "KB";
		}
		if (bb > 1024L)
		{
			bb /= 1024L;
			what = "MB";
		}
		if (bb > 1024L)
		{
			bb /= 1024L;
			what = "GB";
		}
		if (bb > 1024L)
		{
			bb /= 1024L;
			what = "TB";
		}
		
		String tmp = Long.toString(bb);
		int dp = tmp.indexOf('.');
		if (tmp.length() > dp + 2)
			return tmp.substring(0, dp + 3) + " " + what;
		else
			return tmp + " " + what;
	}
	
	private final String getMemory()
	{
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return this.convertBytes(mem);
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		moo.sock.privmsg(target, "[STATUS] " + moo.conf.getNick() + " version " + moo.conf.getVersion() + ". Using " + Thread.activeCount() + " threads and " + this.getMemory() + " of memory");
	}
}