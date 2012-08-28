package net.rizon.moo.antiidle;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class commandIdle extends command
{
	public commandIdle(mpackage pkg)
	{
		super(pkg, "!IDLE", "Excepts people from idle kick");
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !IDLE <nick>");
		moo.notice(source, "The !IDLE command removes the automatic kickban timer for the given nick.");
		moo.notice(source, "This does not create a permanent exception, so if the target leaves the channel");
		moo.notice(source, "through whatever means and joins again, the command must be executed again.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length != 2)
			return;
		
		if (antiIdleEntry.removeTimerFor(params[1]))
			moo.reply(source, target, "Antiidle removed for " + params[1]);
	}
}