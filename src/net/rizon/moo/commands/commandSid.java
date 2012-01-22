package net.rizon.moo.commands;

import java.util.Iterator;
import java.util.Random;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

abstract class commandSidBase extends command
{
	protected commandSidBase(mpackage pkg, final String name, final String desc)
	{
		super(pkg, name, desc);
	}
	
	private static boolean inUse(final String sid)
	{
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();

			if (s.getSID().equalsIgnoreCase(sid))
				return true;
		}
		
		return false;
	}
	
	protected static final Random rand  = new Random();

	protected abstract String getSID();

	@Override
	public void execute(String source, String target, String[] params)
	{
		String sid;
		
		do
			sid = getSID();
		while (inUse(sid));
		
		moo.sock.reply(source, target, "[SID] " + sid);
	}
}

final class commandSidClient extends commandSidBase
{
	public commandSidClient(mpackage pkg)
	{
		super(pkg, "!SID", "Generates a new server ID");
	}
	
	@Override
	protected String getSID()
	{
		int i = rand.nextInt(100);
		String s = Integer.toString(i);
		if (s.length() == 1)
			s = "0" + s;
		s += "C";
		return s;
	}
}

final class commandSidHub extends commandSidBase
{
	public commandSidHub(mpackage pkg)
	{
		super(pkg, "!HUBSID", "Generates a new hub server ID");
	}
	
	@Override
	protected String getSID()
	{
		int i = rand.nextInt(100);
		String s = Integer.toString(i);
		if (s.length() == 1)
			s = "0" + s;
		s += "H";
		return s;
	}
}

public class commandSid
{
	@SuppressWarnings("unused")
	private commandSidClient sid_client;
	@SuppressWarnings("unused")
	private commandSidHub sid_hub;
	
	public commandSid(mpackage pkg)
	{
		this.sid_client = new commandSidClient(pkg);
		this.sid_hub = new commandSidHub(pkg);
	}
}
