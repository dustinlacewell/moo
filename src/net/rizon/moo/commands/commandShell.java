package net.rizon.moo.commands;

import java.io.BufferedReader;
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
			Process proc = Runtime.getRuntime().exec(this.command);
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
		String param = "";
		for (int i = 1; i < params.length; ++i)
			param += params[i];

		shellExec e = new shellExec(target,param);
		e.start();
	}
}
