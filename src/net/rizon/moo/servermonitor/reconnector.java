package net.rizon.moo.servermonitor;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import net.rizon.moo.moo;
import net.rizon.moo.server;
import net.rizon.moo.split;
import net.rizon.moo.timer;

public class reconnector extends timer
{
	private server serv, from;
	private split sp;
	private int tick = 0, tries = 0;
	private HashSet<String> attempted = new HashSet<String>();
	private static long last_reconnect = 0;
	
	public reconnector(server serv, server from)
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
	
	private boolean isGood(server up)
	{
		if (up != null && up.getSplit() == null && up.frozen == false && this.attempted.contains(up.getName()) == false)
			return true;
		return false;
	}
	
	public server findPreferred()
	{
		if (this.from.getSplit() != null && findValidReconnectorFor(this.from) != null)
			return this.serv; // Special case.
		
		LinkedList<server> candidates = new LinkedList<server>();
		
		for (Iterator<String> it = this.serv.preferred_links.iterator(); it.hasNext();)
		{
			final String pname = it.next();
			server pserver = server.findServerAbsolute(pname);
			
			if (isGood(pserver))
				candidates.add(pserver);
		}
		
		if (candidates.isEmpty())
			;
		else if (candidates.size() >= 2 && candidates.get(0).links.size() > candidates.get(1).links.size() * 2)
			return candidates.get(1);
		else
			return candidates.getFirst();
		
		if (isGood(this.from))
			return this.from;
		
		server lowest = null;
		for (Iterator<String> it = this.serv.clines.iterator(); it.hasNext();)
		{
			server altserver = server.findServerAbsolute(it.next());
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
		
		server s = server.findServerAbsolute(this.serv.getName());
		if (s == null || s.getSplit() != this.sp)
		{
			this.destroy();
			this.setRepeating(false);
			return;
		}
		else if (s.frozen || moo.conf.getDisableSplitReconnect())
		{
			for (final String chan : moo.conf.getSplitChannels())
				moo.privmsg(chan, "Disabling reconnect for frozen server " + s.getName());
			
			this.destroy();
			this.setRepeating(false);
			return;
		}
		
		server targ = this.findPreferred();
		if (targ == this.serv) // Special case, hold due to the split probably being between me and serv
		{
			for (final String chan : moo.conf.getSplitChannels())
				moo.privmsg(chan, "Delaying reconnect for " + this.serv.getName() + " due to its uplink being split");
			return;
		}
		
		if (this.tries == 7 || targ == null)
		{
			for (final String chan : moo.conf.getSplitChannels())
				moo.privmsg(chan, "Giving up reconnecting " + s.getName() + ", tried " + this.tries + " times in " + this.tick + " minutes to " + this.attempted.size() + " servers: " + this.attempted.toString());
			
			this.destroy();
			this.setRepeating(false);
			
			return;
		}
		
		int delay = this.serv.isHub() && this.tries == 0 ? 3 : 2;
		
		if (this.tick % delay != 0)
		{
			int wait = 0; 
			for (int i = this.tick; i % delay != 0; ++wait, ++i);
			for (final String chan : moo.conf.getSplitChannels())
				moo.privmsg(chan, "Will reconnect " + s.getName() + " to " + targ.getName() + " in " + wait + " minute" + (wait != 1 ? "s" : ""));
			return;
		}
		
		++this.tries;
		
		for (final String chan : moo.conf.getSplitChannels())
			moo.privmsg(chan, "Reconnect #" + this.tries + " for " + s.getName() + " to " + targ.getName());
		
		moo.sock.write("CONNECT " + s.getName() + " " + moo.conf.getSplitReconnectPort() + " " + targ.getName());
		this.sp.reconnectedBy = moo.conf.getNick();
		
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
	
	private static LinkedList<reconnector> reconnects = new LinkedList<reconnector>();
	
	public static boolean removeReconnectsFor(server s)
	{
		boolean ret = false;
		for (Iterator<reconnector> it = reconnects.iterator(); it.hasNext();)
		{
			reconnector r = it.next();
			if (r.serv == s)
			{
				r.stop();
				it.remove();
				ret = true;
			}
		}
		return ret;
	}
	
	public static reconnector findValidReconnectorFor(server s)
	{
		for (Iterator<reconnector> it = reconnects.iterator(); it.hasNext();)
		{
			reconnector r = it.next();
			if (r.serv == s && r.sp == s.getSplit())
				return r;
		}
		
		return null;
	}
}
