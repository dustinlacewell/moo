package net.rizon.moo.plugin.commands.why;

import com.google.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.plugin.commands.commands;

class DNSBLChecker extends Thread
{
	private CommandSource source;

	private String ip;

	public DNSBLChecker(CommandSource source, final String ip)
	{
		this.source = source;
		this.ip = ip;
	}

	@Override
	public void run()
	{
		String octets[] = ip.split("\\.");
		if (octets.length != 4)
			return;

		String reverse_ip = octets[3] + "." + octets[2] + "." + octets[1] + "." + octets[0];

		for (final String dnsbl : commands.conf.why.servers)
		{
			String lookup_addr = reverse_ip + "." + dnsbl;

			try
			{
				InetAddress.getAllByName(lookup_addr);
				source.reply(this.ip + " is listed in " + dnsbl);
			}
			catch (UnknownHostException ex)
			{
			}
		}
	}
}

public class CommandWhy extends Command
{
	@Inject
	private ServerManager serverManager;

	@Inject
	private Protocol protocol;

	protected static CommandSource command_source;
	public static String host_ip = "", host_host = "";
	public static int requested = 0;

	@Inject
	public CommandWhy(Config conf)
	{
		super("!WHY", "Find why an IP is banned");

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !WHY <ip/host>");
		source.notice("Finds out why a certain IP is banned. It is looked for in DNSBLs and k/K/d:lines");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length <= 1)
		{
			source.reply("Syntax: !WHY <ip/host>");
			return;
		}

		try
		{
			InetAddress addr = InetAddress.getByName(params[1]);
			host_ip = addr.getHostAddress();
			host_host = addr.getHostName();
		}
		catch (UnknownHostException ex)
		{
			source.reply("Invalid IP or host");
			return;
		}


		Thread t = new DNSBLChecker(source, host_ip);
		t.start();

		requested = 0;
		for (Server s : serverManager.getServers())
			if (s.getSplit() == null && !s.isServices() && !s.isHub())
			{
				protocol.write("STATS", "k", s.getName());
				protocol.write("STATS", "K", s.getName());
				protocol.write("STATS", "d", s.getName());
				requested += 3;
			}

		command_source = source;
	}
}
