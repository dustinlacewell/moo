package net.rizon.moo.servermonitor;

import java.util.Date;
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
	private int rtry = 0, ctry = 0;
	
	public reconnector(server serv, server from)
	{
		super(serv.isHub() ? 180 : 120, true);
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
	
	@Override
	public void run(Date now)
	{
		server s = server.findServerAbsolute(this.serv.getName()),
				fr = server.findServer(this.from.getName());
		if (s == null || fr == null || s.getSplit() != this.sp)
		{
			this.destroy();
			this.setRepeating(false);
			return;
		}
		
		this.rtry++;
		
		if (this.rtry > 0 && this.rtry % 3 == 0)
			for (Iterator<String> it = s.clines.iterator(); it.hasNext();)
			{
				server alt = server.findServerAbsolute(it.next());
				if (alt != null && alt != fr && alt.getSplit() == null)
				{
					fr = alt;
					break;
				}
			}

		if (fr.getSplit() != null)
			return;
		
		this.ctry++;
		
		for (final String chan : moo.conf.getSplitChannels())
			moo.privmsg(chan, "Reconnect #" + this.ctry + " for " + s.getName() + " to " + fr.getName());
		
		moo.sock.write("CONNECT " + s.getName() + " " + moo.conf.getSplitReconnectPort() + " " + fr.getName());
		
		if (this.ctry == 3)
		{
			this.destroy();
			this.setRepeating(false);
		}
	}
	
	private static LinkedList<reconnector> reconnects = new LinkedList<reconnector>();
	
	public static void removeReconnectsFor(server s)
	{
		for (Iterator<reconnector> it = reconnects.iterator(); it.hasNext();)
			if (it.next().serv == s)
				it.remove();
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
