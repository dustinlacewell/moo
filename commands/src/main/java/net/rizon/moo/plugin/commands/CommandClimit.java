package net.rizon.moo.plugin.commands;

import java.util.HashSet;

import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

class message_limit extends Message
{
	private static int dashesFor(Server s)
	{
		int longest = 0;
		for (Server s2 : Server.getServers())
		{
			int l = s2.getName().length();
			if (l > longest)
				longest = l;
		}
		
		return longest - s.getName().length() + 2;
	}
	
	public static HashSet<String> waiting_for = new HashSet<String>();
	public static String target_channel = null;
	public static String target_source = null;

	public message_limit(String what)
	{
		super(what);
	}

	@Override
	public void run(String source, String[] message)
	{
		if (target_channel == null || target_source == null)
			return;

		Server s = Server.findServerAbsolute(source);
		if (s == null)
			return;

		String[] tokens = message[1].split(" ");
		for (String token : tokens)
		{
			if (token.startsWith("CHANLIMIT="))
			{				
				if (waiting_for.remove(s.getName()) == false)
					return;

				String limit = token.substring(12);
				String buf = "[CLIMIT] " + s.getName() + " ";
				for (int i = 0, dashes = dashesFor(s); i < dashes; ++i)
					buf += "-";
				buf += " \00309" + limit + "\003";
				
				Moo.reply(target_source, target_channel, buf);
			}
		}
	}
}

class CommandClimit extends Command
{
	@SuppressWarnings("unused")
	private static message_limit message_005 = new message_limit("005");
	@SuppressWarnings("unused")
	private static message_limit message_105 = new message_limit("105");

	public CommandClimit(Plugin pkg)
	{
		super(pkg, "!CLIMIT", "View server channel limits");
		this.requiresChannel(Moo.conf.getList("staff_channels"));
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !CLIMIT");
		Moo.notice(source, "Shows how many channels clients may join for all servers.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (Server s : Server.getServers())
		{
			if (!s.isNormal())
				continue;

			Moo.sock.write("VERSION " + s.getName());
			message_limit.waiting_for.add(s.getName());
		}
		
		message_limit.target_channel = target;
		message_limit.target_source = source;
	}
}