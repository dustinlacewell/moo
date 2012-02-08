package net.rizon.moo.commands;

import net.rizon.moo.mpackage;

public class commands extends mpackage
{
	public commands()
	{
		super("Administation Commands", "Common IRC administration commands");
		
		new commandClimit(this);
		new commandCline(this);
		new commandDnsbl(this);
		new commandFlood(this);
		new commandMap(this);
		new commandOline(this);
		new commandScheck(this);
		new commandSid(this);
		new commandSlackers(this);
		new commandSoa(this);
		new commandTime(this);
		new commandVersions(this);
	}
}