package net.rizon.moo.commands;

import java.util.HashSet;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class message_limit extends message
{
	private static int dashesFor(server s)
	{
		int longest = 0;
		for (server s2 : server.getServers())
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

		server s = server.findServerAbsolute(source);
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
				
				moo.reply(target_source, target_channel, buf);
			}
		}
	}
}

public class commandClimit extends command
{
	@SuppressWarnings("unused")
	private static message_limit message_005 = new message_limit("005");
	@SuppressWarnings("unused")
	private static message_limit message_105 = new message_limit("105");

	public commandClimit(mpackage pkg)
	{
		super(pkg, "!CLIMIT", "View server channel limits"); 
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (server s : server.getServers())
		{
			moo.sock.write("VERSION " + s.getName());
			message_limit.waiting_for.add(s.getName());
		}
		
		message_limit.target_channel = target;
		message_limit.target_source = source;
	}
}