package net.rizon.moo.commands;

import java.util.Random;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;
import net.rizon.moo.Server;

abstract class commandSidBase extends Command
{
	protected commandSidBase(MPackage pkg, final String name, final String desc)
	{
		super(pkg, name, desc);
	}
	
	private static boolean inUse(final String sid)
	{
		for (Server s : Server.getServers())
			if (s.getSID() != null && s.getSID().equalsIgnoreCase(sid))
				return true;
		
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
		
		Moo.reply(source, target, "[SID] " + sid);
	}
}

final class commandSidClient extends commandSidBase
{
	public commandSidClient(MPackage pkg)
	{
		super(pkg, "!SID", "Generates a new server ID");
	}

	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !SID");
		Moo.notice(source, "Generates a new SID for a client server. It will be checked not to be already in use.");
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
	public commandSidHub(MPackage pkg)
	{
		super(pkg, "!HUBSID", "Generates a new hub server ID");
	}
	
	@Override
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: !HUBSID");
		Moo.notice(source, "Generates a new SID for a hub. It will be checked not to be already in use.");
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

class CommandSid
{
	@SuppressWarnings("unused")
	private commandSidClient sid_client;
	@SuppressWarnings("unused")
	private commandSidHub sid_hub;
	
	public CommandSid(MPackage pkg)
	{
		this.sid_client = new commandSidClient(pkg);
		this.sid_hub = new commandSidHub(pkg);
	}
}
