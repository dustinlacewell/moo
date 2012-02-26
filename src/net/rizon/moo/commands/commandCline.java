package net.rizon.moo.commands;

import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class message219_cline extends message
{
	public static HashSet<String> waiting_for = new HashSet<String>();
	public static String message_source, message_target, target_server;
	
	public message219_cline()
	{
		super("219");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equals("c") == false)
			return;
		
		server s = server.findServerAbsolute(source);
		if (s == null)
			return;
		
		if (waiting_for.remove(s.getName()) == false || !waiting_for.isEmpty())
			return;
		
		boolean found = false;
		for (server serv : server.getServers())
		{
			if (!serv.isHub())
				continue;
			for (Iterator<String> it2 = serv.clines.iterator(); it2.hasNext();)
				if (it2.next().equalsIgnoreCase(target_server))
				{
					moo.reply(message_source, message_target, target_server + " can connect to " + serv.getName());
					found = true;
				}
		}
		if (found == false)
			moo.reply(message_source, message_target, target_server + " can't connect to anything, how sad");
	}
}

public class commandCline extends command
{
	@SuppressWarnings("unused")
	private static message219_cline msg_219 = new message219_cline();

	public commandCline(mpackage pkg)
	{
		super(pkg, "!CLINE", "Check where servers can connect to");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
			return;
		
		server serv = server.findServer(params[1]);
		if (serv == null)
		{
			moo.reply(source, target, "Unknown server " + params[1]);
			return;
		}
		
		for (server s : server.getServers())
			if (s.getSplit() == null && !s.isServices())
			{
				s.clines.clear();
				moo.sock.write("STATS c " + s.getName());
				message219_cline.waiting_for.add(s.getName());
			}
		
		message219_cline.message_source = source;
		message219_cline.message_target = target;
		message219_cline.target_server = serv.getName();
	}
}
