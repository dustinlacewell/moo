package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

class shellExec extends Thread
{
	private CommandSource source;
	private String command;

	public shellExec(CommandSource source, final String command)
	{
		this.source = source;
		this.command = command;
	}

	@Override
	public void run()
	{
		try
		{
			Process proc = Runtime.getRuntime().exec(this.command, null, new File(core.conf.shell.base));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			for (String line; (line = in.readLine()) != null;)
			{
				if (line.startsWith("!MOO!SHUTDOWN "))
				{
					source.reply("Caught shutdown signal.");
					Moo.sock.write("QUIT :" + line.substring(14));
					Moo.quitting = true;
					break;
				}

				source.reply(line);
			}

			in.close();
			proc.getOutputStream().close();
			proc.getErrorStream().close();
		}
		catch (IOException ex)
		{
			source.reply("Error running command");
		}
	}
}

class CommandShell extends Command
{
	public CommandShell(Plugin pkg)
	{
		super(pkg, "!SHELL", "Execute a shell command");
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("!SHELL <command>");
		source.notice("!SHELL executes a single shell command from the configured shell base directory.");
		source.notice("This command is currently " + (core.conf.shell.enabled ? "enabled" : "disabled") + ".");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (!core.conf.shell.enabled || params.length == 1)
			return;

		File base = new File(core.conf.shell.base);
		if (base.exists() == false || base.isDirectory() == false)
		{
			source.reply("Shell base dir is set to an invalid path");
			return;
		}

		String param = "./";
		for (int i = 1; i < params.length; ++i)
			param += params[i];

		if (param.indexOf("..") != -1)
			return;

		shellExec e = new shellExec(source, param);
		e.start();
	}
}
