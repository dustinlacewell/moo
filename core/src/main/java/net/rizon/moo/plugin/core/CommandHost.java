package net.rizon.moo.plugin.core;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

import java.net.InetAddress;
import java.net.UnknownHostException;

class CommandHost extends Command
{
	public CommandHost(Plugin pkg)
	{
		super(pkg, "!HOST", "Resolve a hostname");

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length <= 1)
			return;
		
		try
		{
			for (InetAddress in : InetAddress.getAllByName(params[1]))
				source.reply(params[1] + " has address " + in.getHostAddress());
		}
		catch (UnknownHostException ex)
		{
			source.reply(params[1] + " does not resolve.");
		}
	}
}