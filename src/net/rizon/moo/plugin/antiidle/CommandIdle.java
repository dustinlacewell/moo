package net.rizon.moo.plugin.antiidle;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class CommandIdle extends Command
{
	public CommandIdle(Plugin pkg)
	{
		super(pkg, "!IDLE", "Excepts people from idle kick");
		this.requiresChannel(Moo.conf.getList("staff_channels"));
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !IDLE <nick>");
		Moo.notice(source, "The !IDLE command removes the automatic kickban timer for the given nick.");
		Moo.notice(source, "This does not create a permanent exception, so if the target leaves the channel");
		Moo.notice(source, "through whatever means and joins again, the command must be executed again.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length != 2)
			return;
		
		if (AntiIdleEntry.removeTimerFor(params[1]))
			Moo.reply(source, target, "Antiidle removed for " + params[1]);
	}
}