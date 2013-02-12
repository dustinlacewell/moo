package net.rizon.moo.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

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

	public void run()
	{
		try
		{
			Process proc = Runtime.getRuntime().exec(this.command, null, new File(moo.conf.getShellBase()));
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			for (String line; (line = in.readLine()) != null;)
			{
				if (line.startsWith("!MOO!SHUTDOWN "))
				{
					moo.reply(this.source, this.target, "Caught shutdown signal.");
					moo.sock.write("QUIT :" + line.substring(14));
					moo.quitting = true;
					break;
				}
				
				moo.reply(this.source, this.target, line);
			}
			
			in.close();
		}
		catch (IOException ex)
		{
			moo.reply(this.source, this.target, "Error running command");
		}
	}
}

class commandShell extends command
{
	public commandShell(mpackage pkg)
	{
		super(pkg, "!SHELL", "Execute a shell command");
		this.requiresChannel(moo.conf.getAdminChannels());
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "!SHELL <command>");
		moo.notice(source, "!SHELL executes a single shell command from the configured shell base directory.");
		moo.notice(source, "This command is currently " + (moo.conf.getShell() ? "enabled" : "disabled") + ".");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (moo.conf.getShell() == false || params.length == 1)
			return;
		
		File base = new File(moo.conf.getShellBase());
		if (base.exists() == false || base.isDirectory() == false)
		{
			moo.reply(source, target, "Shell base dir is set to an invalid path");
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
