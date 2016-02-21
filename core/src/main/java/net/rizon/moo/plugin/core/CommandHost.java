package net.rizon.moo.plugin.core;

import com.google.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;

class CommandHost extends Command
{
	@Inject
	CommandHost(Config conf)
	{
		super("!HOST", "Resolve a hostname");

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
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