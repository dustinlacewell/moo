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
	public void execute(String source, String target, String[] params)
	{
		if (params.length != 2)
			return;
		
		if (antiIdleEntry.removeTimerFor(params[1]))
			moo.reply(source, target, "Antiidle removed for " + params[1]);
	}
}