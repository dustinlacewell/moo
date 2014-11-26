package net.rizon.moo.plugin.antiidle;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class CommandIdle extends Command
{
	public CommandIdle(Plugin pkg)
	{
		super(pkg, "!IDLE", "Excepts people from idle kick");
		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !IDLE <nick>");
		source.notice("The !IDLE command removes the automatic kickban timer for the given nick.");
		source.notice("This does not create a permanent exception, so if the target leaves the channel");
		source.notice("through whatever means and joins again, the command must be executed again.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length != 2)
			return;

		if (AntiIdleEntry.removeTimerFor(params[1]))
			source.reply("Antiidle removed for " + params[1]);
	}
}