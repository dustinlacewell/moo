package net.rizon.moo.commands;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import net.rizon.moo.command;
import net.rizon.moo.moo;

class shellExec extends Thread
{
	private String target;
	private String command;

	public shellExec(final String target, final String command)
	{
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
				moo.sock.privmsg(this.target, line);
		}
		catch (IOException ex)
		{
			moo.sock.privmsg(target, "Error running command");
		}
	}
}

public class commandShell extends command
{
	public commandShell()
	{
		super("!SHELL");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (moo.conf.getShell() == false || moo.conf.isAdminChannel(target) == false || params.length == 1)
			return;
		
		File base = new File(moo.conf.getShellBase());
		if (base.exists() == false || base.isDirectory() == false)
		{
			moo.sock.privmsg(target, "Shell base dir is set to an invalid path");
			return;
		}

		String param = "";
		for (int i = 1; i < params.length; ++i)
			param += params[i];
		
		if (param.indexOf("..") != -1)
			return;

		shellExec e = new shellExec(target, param);
		e.start();
	}
}
