package net.rizon.moo.commands;

import net.rizon.moo.mpackage;

public class commands extends mpackage
{
	public commands()
	{
		super("Commands", "Core commands");
		
		new commandClimit(this);
		new commandCline(this);
		new commandDnsbl(this);
		new commandFlood(this);
		new commandHelp(this);
		new commandMap(this);
		new commandOline(this);
		new commandReload(this);
		new commandScheck(this);
		new commandShell(this);
		new commandShutdown(this);
		new commandSid(this);
		new commandSlackers(this);
		new commandSoa(this);
		new commandSplit(this);
		new commandStatus(this);
		new commandTime(this);
		new commandVersions(this);
	}
}