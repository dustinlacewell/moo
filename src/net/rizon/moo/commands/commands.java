package net.rizon.moo.commands;

import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

public class commands extends MPackage
{
	public commands()
	{
		super("Administation Commands", "Common IRC administration commands");
		
		if (Moo.conf.getProtocol().equals("plexus"))
		{
			new CommandClimit(this);
			new CommandDnsbl(this);
			new CommandMap(this);
			new CommandOline(this);
		}
		if (Moo.conf.getProtocol().equals("plexus"))
		{
			new CommandSid(this);
			new CommandSlackers(this);
		}
		new CommandSoa(this);
		new CommandTime(this);
		new CommandUptime(this);
		if (Moo.conf.getProtocol().equals("plexus"))
		{
			new CommandVersions(this);
			new CommandWhy(this);
		}
	}
}