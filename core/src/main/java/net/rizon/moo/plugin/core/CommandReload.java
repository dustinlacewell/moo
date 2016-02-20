package net.rizon.moo.plugin.core;


import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import net.rizon.moo.conf.Configuration;
import net.rizon.moo.events.OnReload;
import org.slf4j.Logger;

class CommandReload extends Command
{
	@Inject
	private static Logger logger;

	@Inject
	private EventBus eventBus;

	public CommandReload(Plugin pkg)
	{
		super(pkg, "!RELOAD", "Reloads the configuration file");
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !RELOAD");
		source.notice("!RELOAD reloads the configuration file, moo.yml.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		try
		{
			Config c = Configuration.load("moo.yml", Config.class);
			
			eventBus.post(new OnReload(source));

			Moo.conf = c;
			source.reply("Successfully reloaded configuration");
		}
		catch (Exception ex)
		{
			source.reply("Error reloading configuration: " + ex.getMessage());
			
			logger.warn("Error reloading configuration", ex);
		}
	}
}