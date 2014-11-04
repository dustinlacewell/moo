package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.Event;
import net.rizon.moo.Logger;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.core.conf.CoreConfiguration;

public class core extends Plugin
{
	public static CoreConfiguration conf;

	protected static final Logger log = Logger.getLogger(core.class.getName());

	private CommandHelp help;
	private Command host, plugins, rand, reload, shell, shutdown, status;
	private Event e;
	
	public core() throws Exception
	{
		super("Commands", "Core commands");
		conf = CoreConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		help = new CommandHelp(this);
		host = new CommandHost(this);
		plugins = new CommandPlugins(this);
		rand = new CommandRand(this);
		reload = new CommandReload(this);
		shell = new CommandShell(this);
		shutdown = new CommandShutdown(this);
		status = new CommandStatus(this);

		e = new EventCore();
	}

	@Override
	public void stop()
	{
		help.remove();
		host.remove();
		plugins.remove();
		rand.remove();
		reload.remove();
		shell.remove();
		shutdown.remove();
		status.remove();
		e.remove();
	}
}
