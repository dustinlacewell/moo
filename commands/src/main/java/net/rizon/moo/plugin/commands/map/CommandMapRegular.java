package net.rizon.moo.plugin.commands.map;

import com.google.inject.Inject;
import net.rizon.moo.conf.Config;

public class CommandMapRegular extends CommandMap
{
	@Inject
	public CommandMapRegular(Config conf)
	{
		super(conf, "!MAP", false);
	}
}