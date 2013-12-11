package net.rizon.moo.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

class CommandHost extends Command
{
	public CommandHost(MPackage pkg)
	{
		super(pkg, "!HOST", "Resolve a hostname");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 1)
			return;
		
		try
		{
			for (InetAddress in : InetAddress.getAllByName(params[1]))
				Moo.reply(source, target, params[1] + " has address " + in.getHostAddress());
		}
		catch (UnknownHostException ex)
		{
			Moo.reply(source, target, params[1] + " does not resolve.");
		}
	}
}