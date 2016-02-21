package net.rizon.moo.plugin.core;

import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.core.conf.CoreConfiguration;

class shellExec extends Thread
{
	private CoreConfiguration conf;
	private Protocol protocol;
	private CommandSource source;
	private String command;

	public shellExec(CoreConfiguration conf, Protocol protocol, CommandSource source, String command)
	{
		this.conf = conf;
		this.protocol = protocol;
		this.source = source;
		this.command = command;
	}

	@Override
	public void run()
	{
		try
		{
			Process proc = Runtime.getRuntime().exec(this.command, null, new File(conf.shell.base));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			for (String line; (line = in.readLine()) != null;)
			{
				if (line.startsWith("!MOO!SHUTDOWN "))
				{
					source.reply("Caught shutdown signal.");
					protocol.write("QUIT", line.substring(14));
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
	@Inject
	private Protocol protocol;

	@Inject
	private CoreConfiguration conf;

	@Inject
	CommandShell(Config conf)
	{
		super("!SHELL", "Execute a shell command");
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("!SHELL <command>");
		source.notice("!SHELL executes a single shell command from the configured shell base directory.");
		source.notice("This command is currently " + (conf.shell.enabled ? "enabled" : "disabled") + ".");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (!conf.shell.enabled || params.length == 1)
			return;

		File base = new File(conf.shell.base);
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

		shellExec e = new shellExec(conf, protocol, source, param);
		e.start();
	}
}
