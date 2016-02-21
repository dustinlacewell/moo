package net.rizon.moo.plugin.antiidle;

import com.google.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;

class CommandIdle extends Command
{
	@Inject
	CommandIdle(Config conf)
	{
		super("!IDLE", "Excepts people from idle kick");
		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
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