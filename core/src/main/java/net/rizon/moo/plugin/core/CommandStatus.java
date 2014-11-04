package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Version;

class CommandStatus extends Command
{
	public CommandStatus(Plugin pkg)
	{
		super(pkg, "!STATUS", "View " + Moo.conf.general.nick + "'s status");
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
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !STATUS");
		source.notice("!STATUS prints misc info on " + Moo.conf.general.nick + ".");
		source.notice("This includes the version, the date when " + Moo.conf.general.nick + " was started,");
		source.notice("the amount of currently running threads and memory usage.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		source.reply("[STATUS] " + Moo.conf.general.nick + " version " + Moo.conf.version + ", created on " + Moo.getCreated() + ". Revision " + Version.getFullVersion() + ". Using " + Thread.activeCount() + " threads and " + this.getMemory() + " of memory");
	}
}