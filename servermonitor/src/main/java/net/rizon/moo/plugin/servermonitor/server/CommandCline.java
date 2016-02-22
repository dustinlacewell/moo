package net.rizon.moo.plugin.servermonitor.server;

import com.google.inject.Inject;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;

public class CommandCline extends CommandServerBase
{
	@Inject
	public CommandCline(Config conf)
	{
		super(conf, "!CLINE");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length == 2)
			super.execute(source, params);
	}
}