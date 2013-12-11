package net.rizon.moo.core;

import net.rizon.moo.Command;
import net.rizon.moo.Plugin;

public class core extends Plugin
{
	private CommandHelp help;
	private Command host, plugins, rand, reload, shell, shutdown, status;
	
	public core()
	{
		super("Commands", "Core commands");
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
	}
}
