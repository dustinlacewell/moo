package net.rizon.moo.commands;

import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class message005 extends message
{
	private static int dashesFor(server s)
	{
		int longest = 0;
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			int l = it.next().getName().length();
			if (l > longest)
				longest = l;
		}
		
		return longest - s.getName().length() + 2;
	}
	
	public static HashSet<String> waiting_for = new HashSet<String>();
	public static String target_channel = null;
	public static String target_source = null;

	public message005(String what)
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
				
				moo.sock.reply(target_source, target_channel, buf);
			}
		}
	}
}

public class commandClimit extends command
{
	@SuppressWarnings("unused")
	private static message005 message_005 = new message005("005");
	@SuppressWarnings("unused")
	private static message005 message_105 = new message005("105");

	public commandClimit(mpackage pkg)
	{
		super(pkg, "!CLIMIT", "View server channel limits"); 
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();
			moo.sock.write("VERSION " + s.getName());
			message005.waiting_for.add(s.getName());
		}
		
		message005.target_channel = target;
		message005.target_source = source;
	}
}