package net.rizon.moo.plugin.core;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

class CommandHost extends Command
{
	public CommandHost(Plugin pkg)
	{
		super(pkg, "!HOST", "Resolve a hostname");
		
		this.requiresChannel(Moo.conf.getList("staff_channels"));
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
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