package net.rizon.moo.plugin.commands.map;

import com.google.inject.Inject;
import net.rizon.moo.conf.Config;

public class CommandMapAll extends CommandMap
{
	@Inject
	public CommandMapAll(Config conf)
	{
		super(conf, "!MAP-" , true);
	}
}