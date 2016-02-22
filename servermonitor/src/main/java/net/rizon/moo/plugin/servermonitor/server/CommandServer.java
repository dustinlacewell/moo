package net.rizon.moo.plugin.servermonitor.server;

import com.google.inject.Inject;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;

public class CommandServer extends CommandServerBase
{
	@Inject
	public CommandServer(Config conf)
	{
		super(conf, "!SERVER");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length == 2)
			super.execute(source, params);
	}
}
