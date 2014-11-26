package net.rizon.moo.plugin.commands;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Protocol;

public class commands extends Plugin
{
	private Command climit, oline, slackers, soa, time, uptime, why;
	private CommandMap map;
	private CommandSid sid;
	private CommandVersions version;

	public commands()
	{
		super("Administation Commands", "Common IRC administration commands");
	}

	@Override
	public void start() throws Exception
	{
		if (Moo.conf.general.protocol == Protocol.PLEXUS)
		{
			climit = new CommandClimit(this);
			map = new CommandMap(this);
			oline = new CommandOline(this);
			sid = new CommandSid(this);
			slackers = new CommandSlackers(this);
		}
		soa = new CommandSoa(this);
		time = new CommandTime(this);
		uptime = new CommandUptime(this);
		if (Moo.conf.general.protocol == Protocol.PLEXUS)
		{
			version = new CommandVersions(this);
			why = new CommandWhy(this);
		}
	}

	@Override
	public void stop()
	{
		if (climit != null)
			climit.remove();
		if (map != null)
			map.remove();
		if (oline != null)
			oline.remove();
		if (sid != null)
			sid.remove();
		if (slackers != null)
			slackers.remove();
		this.soa.remove();
		this.time.remove();
		this.uptime.remove();
		if (version != null)
			this.version.remove();
		if (why != null)
			this.why.remove();
	}
}
