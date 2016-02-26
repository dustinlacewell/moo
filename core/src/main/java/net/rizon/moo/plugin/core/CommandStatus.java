package net.rizon.moo.plugin.core;

import com.google.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Version;
import net.rizon.moo.conf.Config;

class CommandStatus extends Command
{
	@Inject
	private Config conf;

	@Inject
	CommandStatus(Config conf)
	{
		super("!STATUS", "View " + conf.general.nick + "'s status");
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

	private String getMemory()
	{
		long mem = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
		return this.convertBytes(mem);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !STATUS");
		source.notice("!STATUS prints misc info on " + conf.general.nick + ".");
		source.notice("This includes the version, the date when " + conf.general.nick + " was started,");
		source.notice("the amount of currently running threads and memory usage.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		source.reply("[STATUS] " + conf.general.nick + " version " + conf.version + ", started on " + Moo.getCreated() + ", revision " + Version.getRevision() + ". Using " + Thread.activeCount() + " threads and " + this.getMemory() + " of memory");
	}
}