package net.rizon.moo.servermonitor;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.Split;
import net.rizon.moo.Timer;

class Reconnector extends Timer
{
	private Server serv, from;
	private Split sp;
	private int tick = 0, tries = 0;
	private HashSet<String> attempted = new HashSet<String>();
	private static long last_reconnect = 0;
	
	public Reconnector(Server serv, Server from)
	{
		super(60, true);
		this.serv = serv;
		this.from = from;
		this.sp = serv.getSplit();
		
		reconnects.add(this);
	}
	
	public void destroy()
	{
		this.stop();
		reconnects.remove(this);
	}
	
	private boolean isGood(Server up)
	{
		if (up != null && up.getSplit() == null && up.frozen == false && this.attempted.contains(up.getName()) == false)
			return true;
		return false;
	}
	
	public Server findPreferred()
	{
		if (this.from.getSplit() != null && findValidReconnectorFor(this.from) != null)
			return this.serv; // Special case.
		
		for (Iterator<String> it = this.serv.preferred_links.iterator(); it.hasNext();)
		{
			final String pname = it.next();
			Server pserver = Server.findServerAbsolute(pname);
			
			if (isGood(pserver))
				return pserver;
		}
		
		if (isGood(this.from))
			return this.from;
		
		Server lowest = null;
		for (Iterator<String> it = this.serv.clines.iterator(); it.hasNext();)
		{
			Server altserver = Server.findServerAbsolute(it.next());
			if (isGood(altserver) && altserver.isHub()
					&& (lowest == null || altserver.links.size() < lowest.links.size()))
				lowest = altserver;
		}
		
		return lowest;
	}
	
	@Override
	public void run(Date now)
	{
		if (last_reconnect + 60 > System.currentTimeMillis() / 1000L)
			return;
		
		++this.tick;
		
		Server s = Server.findServerAbsolute(this.serv.getName());
		if (s == null || this.sp == null || !this.sp.equals(s.getSplit()))
		{
			this.destroy();
			this.setRepeating(false);
			return;
		}
		else if (Moo.conf.getBool("disable_split_reconnect"))
		{
			for (final String chan : Moo.conf.getList("split_channels"))
				Moo.privmsg(chan, "Disabling reconnect for frozen server " + s.getName());
			
			this.destroy();
			this.setRepeating(false);
			return;
		}
		
		Server targ = this.findPreferred();
		if (targ == this.serv) // Special case, hold due to the split probably being between me and serv
		{
			for (final String chan : Moo.conf.getList("split_channels"))
				Moo.privmsg(chan, "Delaying reconnect for " + this.serv.getName() + " due to its uplink being split");
			return;
		}
		
		if (this.tries == 7 || targ == null)
		{
			for (final String chan : Moo.conf.getList("split_channels"))
				Moo.privmsg(chan, "Giving up reconnecting " + s.getName() + ", tried " + this.tries + " times in " + this.tick + " minutes to " + this.attempted.size() + " servers: " + this.attempted.toString());
			
			this.destroy();
			this.setRepeating(false);
			
			return;
		}
		
		int delay = this.serv.isHub() && this.tries == 0 ? 3 : 2;
		
		if (this.tick % delay != 0)
		{
			int wait = 0; 
			for (int i = this.tick; i % delay != 0; ++wait, ++i);
			for (final String chan : Moo.conf.getList("split_channels"))
				Moo.privmsg(chan, "Will reconnect " + s.getName() + " to " + targ.getName() + " in " + wait + " minute" + (wait != 1 ? "s" : ""));
			return;
		}
		
		++this.tries;
		
		for (final String chan : Moo.conf.getList("split_channels"))
			Moo.privmsg(chan, "Reconnect #" + this.tries + " for " + s.getName() + " to " + targ.getName());
		
		Moo.sock.write("CONNECT " + s.getName() + " " + Moo.conf.getInt("split_reconnect_port") + " " + targ.getName());
		this.sp.reconnectedBy = Moo.conf.getString("nick");
		
		last_reconnect = System.currentTimeMillis() / 1000L;
		if (this.tries != 1) // Allow two tries on the first server
			this.attempted.add(targ.getName());
	}
	
	public Date reconnectTime()
	{
		int delay = this.serv.isHub() ? 3 : 2;
		int wait = 0;
		for (int i = this.tick + 1; i % delay != 0; ++wait, ++i);
		wait *= 60;
		wait += (int) ((this.getTick().getTime() - System.currentTimeMillis()) / 1000L);
		
		return new Date(System.currentTimeMillis() + (wait * 1000L));
	}
	
	private static LinkedList<Reconnector> reconnects = new LinkedList<Reconnector>();
	
	public static boolean removeReconnectsFor(Server s)
	{
		boolean ret = false;
		for (Iterator<Reconnector> it = reconnects.iterator(); it.hasNext();)
		{
			Reconnector r = it.next();
			if (r.serv == s)
			{
				r.stop();
				it.remove();
				ret = true;
			}
		}
		return ret;
	}
	
	public static Reconnector findValidReconnectorFor(Server s)
	{
		for (Iterator<Reconnector> it = reconnects.iterator(); it.hasNext();)
		{
			Reconnector r = it.next();
			if (r.serv == s && r.sp != null && r.sp.equals(s.getSplit()))
				return r;
		}
		
		return null;
	}
}
