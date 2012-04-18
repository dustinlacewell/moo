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
		
		server targ = server.findServer(params[1]);
		if (targ == null)
		{
			moo.reply(source, target, "Unknown server " + params[1]);
			return;
		}
		
		boolean found = false;
		for (server serv : server.getServers())
		{
			if (!serv.isHub())
				continue;
			for (Iterator<String> it2 = serv.clines.iterator(); it2.hasNext();)
				if (it2.next().equalsIgnoreCase(targ.getName()))
				{
					moo.reply(source, target, targ.getName() + " can connect to " + serv.getName());
					found = true;
				}
		}
		if (found == false)
			moo.reply(source, target, targ.getName() + " can't connect to anything, how sad");
	}
}
