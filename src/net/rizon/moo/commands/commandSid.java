package net.rizon.moo.commands;

import java.util.Iterator;
import java.util.Random;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class commandSid extends command
{
	public commandSid()
	{
		super("!NEW-SID", "Generates a new server ID");
	}
	
	private boolean inUse(final String sid)
	{
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();

			if (s.getSID().equalsIgnoreCase(sid))
				return true;
		}
		
		return false;
	}
	
	private static final Random rand  = new Random();

	private String getSID()
	{
		int i = rand.nextInt(100);
		String s = Integer.toString(i);
		if (s.length() == 1)
			s = "0" + s;
		s += "C";
		return s;
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		String sid;
		
		do
			sid = getSID();
		while (inUse(sid));
		
		moo.sock.privmsg(target, "[SID] " + sid);
	}
}