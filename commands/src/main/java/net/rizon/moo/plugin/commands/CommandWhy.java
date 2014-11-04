package net.rizon.moo.plugin.commands;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

import java.net.InetAddress;
import java.net.UnknownHostException;

class DNSBLChecker extends Thread
{
	private static final String DNSBLs[] = { "rbl.efnetrbl.org", "dnsbl.dronebl.org" };

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
		
		for (final String dnsbl : DNSBLs)
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

class message_216 extends Message
{
	public message_216()
	{
		super("216");
	}

	@Override
	public void run(String source, String[] message) 
	{
		if (message[1].equals("k") == false && message[1].equals("K") == false)
			return;
		else if (CommandWhy.host_ip.isEmpty())
			return;
		else if (message[2].equalsIgnoreCase(CommandWhy.host_ip) == false && message[2].equalsIgnoreCase(CommandWhy.host_host) == false)
			return;

		CommandWhy.command_source.reply("[" + source + "] " + message[2] + " is " + message[1] + "-lined for: " + message[5]);

		CommandWhy.host_ip = "";
		CommandWhy.host_host = "";
	}
}

class message_225 extends Message
{
	public message_225()
	{
		super("225");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equals("d") == false)
			return;
		else if (CommandWhy.host_ip.isEmpty())
			return;
		else if (message[2].equalsIgnoreCase(CommandWhy.host_ip) == false && message[2].equalsIgnoreCase(CommandWhy.host_host) == false)
			return;

		CommandWhy.command_source.reply("[" + source + "] " + message[2] + " is " + message[1] + "-lined for: " + message[3]);

		CommandWhy.host_ip = "";
		CommandWhy.host_host = "";
	}
}

class message219_why extends Message
{
	public message219_why()
	{
		super("219");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (CommandWhy.host_ip.isEmpty())
			return;
		
		CommandWhy.requested--;
		
		if (CommandWhy.requested == 0)
		{
			CommandWhy.command_source.reply(CommandWhy.host_ip + " (" + CommandWhy.host_host + ") is not banned");
			
			CommandWhy.host_ip = "";
			CommandWhy.host_host = "";
		}
	}
}

class CommandWhy extends Command
{
	@SuppressWarnings("unused")
	private static final message_216 message216 = new message_216();
	@SuppressWarnings("unused")
	private static final message_225 message225 = new message_225();
	@SuppressWarnings("unused")
	private static final message219_why message219 = new message219_why();

	protected static CommandSource command_source;
	public static String host_ip = "", host_host = "";
	public static int requested = 0;

	public CommandWhy(Plugin pkg)
	{
		super(pkg, "!WHY", "Find why an IP is banned");
		
		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
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
		for (Server s : Server.getServers())
			if (s.getSplit() == null && !s.isServices() && !s.isHub())
			{
				Moo.sock.write("STATS k " + s.getName());
				Moo.sock.write("STATS K " + s.getName());
				Moo.sock.write("STATS d " + s.getName());
				requested += 3;
			}

		command_source = source;
	}
}
