package net.rizon.moo.commands;

import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

public class commandCline extends command
{
	public commandCline(mpackage pkg)
	{
		super(pkg, "!CLINE", "Check where servers can connect to");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
			return;
		
		server s = server.findServer(params[1]);
		if (s == null)
		{
			moo.sock.reply(source, target, "Unknown server " + params[1]);
			return;
		}
		
		boolean found = false;
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server serv = it.next();
			if (!serv.isHub())
				continue;
			for (Iterator<String> it2 = serv.clines.iterator(); it2.hasNext();)
				if (it2.next().equalsIgnoreCase(s.getName()))
				{
					moo.sock.reply(source, target, s.getName() + " can connect to " + serv.getName());
					found = true;
				}
		}
		if (found == false)
			moo.sock.reply(source, target, s.getName() + " can't connect to anything, how sad");
	}
}
