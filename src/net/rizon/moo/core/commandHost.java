package net.rizon.moo.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

class commandHost extends command
{
	public commandHost(mpackage pkg)
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
				moo.reply(source, target, params[1] + " has address " + in.getHostAddress());
		}
		catch (UnknownHostException ex)
		{
			moo.reply(source, target, params[1] + " does not resolve.");
		}
	}
}