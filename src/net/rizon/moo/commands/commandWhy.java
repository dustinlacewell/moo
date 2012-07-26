package net.rizon.moo.commands;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class DNSBLChecker extends Thread
{
	private static final String DNSBLs[] = { "rbl.efnetrbl.org", "dnsbl.rizon.net" };
	
	private String source, target;
	
	private String ip;
	
	public DNSBLChecker(final String source, final String target, final String ip)
	{
		this.source = source;
		this.target = target;
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
				moo.reply(this.source, this.target, this.ip + " is listed in " + dnsbl);
			}
			catch (UnknownHostException ex)
			{
			}
		}
	}
}

class message_216 extends message
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
		else if (commandWhy.host_ip.isEmpty())
			return;
		else if (message[2].equalsIgnoreCase(commandWhy.host_ip) == false && message[2].equalsIgnoreCase(commandWhy.host_host) == false)
			return;
		
		moo.reply(commandWhy.message_source, commandWhy.message_target, "[" + source + "] " + message[2] + " is " + message[1] + "-lined for: " + message[5]);

		commandWhy.host_ip = "";
		commandWhy.host_host = "";
	}
}

class message_225 extends message
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
		else if (commandWhy.host_ip.isEmpty())
			return;
		else if (message[2].equalsIgnoreCase(commandWhy.host_ip) == false && message[2].equalsIgnoreCase(commandWhy.host_host) == false)
			return;
		
		moo.reply(commandWhy.message_source, commandWhy.message_target, "[" + source + "] " + message[2] + " is " + message[1] + "-lined for: " + message[3]);

		commandWhy.host_ip = "";
		commandWhy.host_host = "";
	}
}

class message219_why extends message
{
	public message219_why()
	{
		super("219");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (commandWhy.host_ip.isEmpty())
			return;
		
		commandWhy.requested--;
		
		if (commandWhy.requested == 0)
		{
			moo.reply(commandWhy.message_source, commandWhy.message_target, commandWhy.host_ip + " (" + commandWhy.host_host + ") is not banned");
			
			commandWhy.host_ip = "";
			commandWhy.host_host = "";
		}
	}
}

class commandWhy extends command
{
	@SuppressWarnings("unused")
	private static final message_216 message216 = new message_216();
	@SuppressWarnings("unused")
	private static final message_225 message225 = new message_225();
	@SuppressWarnings("unused")
	private static final message219_why message219 = new message219_why();
	
	public static String message_target, message_source, host_ip = "", host_host = "";
	public static int requested = 0;

	public commandWhy(mpackage pkg)
	{
		super(pkg, "!WHY", "Find why an IP is banned");
	}

	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !WHY <ip/host>");
		moo.notice(source, "Finds out why a certain IP is banned. It is looked for in DNSBLs and k/K/d:lines");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 1)
		{
			moo.reply(source, target, "Syntax: !WHY <ip/host>");
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
			moo.reply(source, target, "Invalid IP or host");
			return;
		}
		
		
		Thread t = new DNSBLChecker(source, target, host_ip);
		t.start();
		
		requested = 0;
		for (server s : server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				moo.sock.write("STATS k " + s.getName());
				moo.sock.write("STATS K " + s.getName());
				moo.sock.write("STATS d " + s.getName());
				requested += 3;
			}
		
		message_target = target;
		message_source = source;
	}
}
