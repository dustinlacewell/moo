package net.rizon.moo.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class shellExec extends Thread
{
	private String source;
	private String target;
	private String command;

	public shellExec(final String source, final String target, final String command)
	{
		this.source = source;
		this.target = target;
		this.command = command;
	}

	@Override
	public void run()
	{
		try
		{
			Process proc = Runtime.getRuntime().exec(this.command, null, new File(Moo.conf.getString("shell_base")));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			for (String line; (line = in.readLine()) != null;)
			{
				if (line.startsWith("!MOO!SHUTDOWN "))
				{
					Moo.reply(this.source, this.target, "Caught shutdown signal.");
					Moo.sock.write("QUIT :" + line.substring(14));
					Moo.quitting = true;
					break;
				}
				
				Moo.reply(this.source, this.target, line);
			}
			
			in.close();
			proc.getOutputStream().close();
			proc.getErrorStream().close();
		}
		catch (IOException ex)
		{
			Moo.reply(this.source, this.target, "Error running command");
		}
	}
}

class CommandShell extends Command
{
	public CommandShell(Plugin pkg)
	{
		super(pkg, "!SHELL", "Execute a shell command");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "!SHELL <command>");
		Moo.notice(source, "!SHELL executes a single shell command from the configured shell base directory.");
		Moo.notice(source, "This command is currently " + (Moo.conf.getBool("enable_shell") ? "enabled" : "disabled") + ".");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (Moo.conf.getBool("enable_shell") == false || params.length == 1)
			return;
		
		File base = new File(Moo.conf.getString("shell_base"));
		if (base.exists() == false || base.isDirectory() == false)
		{
			Moo.reply(source, target, "Shell base dir is set to an invalid path");
			return;
		}

		String param = "./";
		for (int i = 1; i < params.length; ++i)
			param += params[i];
		
		if (param.indexOf("..") != -1)
			return;

		shellExec e = new shellExec(source, target, param);
		e.start();
	}
}
