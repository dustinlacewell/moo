package net.rizon.moo.plugin.commands.version;

import com.google.inject.Inject;
import net.rizon.moo.conf.Config;


public class CommandVersion extends CommandVersionBase
{
	@Inject
	public CommandVersion(Config conf, String command)
	{
		super(conf, "!VERSION");
	}
}