package net.rizon.moo.plugin.commands.version;

import com.google.inject.Inject;
import net.rizon.moo.conf.Config;


public class CommandVersions extends CommandVersionBase
{
	@Inject
	public CommandVersions(Config conf, String command)
	{
		super(conf, "!VERSIONS");
	}
}