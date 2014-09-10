package net.rizon.moo.plugin.dnsblstats;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;

public class EventDnsbl extends Event
{
	@Override
	public void onConnect()
	{
		for (Server s : Server.getServers())
			Moo.sock.write("STATS B " + s.getName());
	}
	
	@Override
	public void onServerLink(Server serv, Server to)
	{
		/* Be sure dnsbl stats are up to date, prevents long splits from tripping the dnsbl monitor */
		Moo.sock.write("STATS B " + serv.getName());
	}
	
	@Override
	public void onServerDestroy(Server serv)
	{
		dnsblstats.infos.remove(serv);
	}
}
