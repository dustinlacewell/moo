package net.rizon.moo.plugin.servermonitor;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.Split;

class Reconnector implements Runnable
{
	private Server serv, from;
	private Split sp;
	private int tick = 0, tries = 0;
	private HashSet<String> attempted = new HashSet<String>();
	private static long last_reconnect = 0;
	protected ScheduledFuture future;

	public Reconnector(Server serv, Server from)
	{
		this.serv = serv;
		this.from = from;
		this.sp = serv.getSplit();

		reconnects.add(this);
	}

	public void destroy()
	{
		future.cancel(false);
		reconnects.remove(this);
	}

	private boolean isGood(Server up)
	{
		return up != null && up.getSplit() == null && up.frozen == false && this.attempted.contains(up.getName()) == false && !hasSplitBefore(up);
	}

	private boolean hasSplitBefore(Server s)
	{
		Split[] splits = this.serv.getSplits();
		if (splits.length < 2)
			return false;

		// If the two most recent split were from 's' and they were <10 and <20, move the server

		Split last = splits[splits.length - 1];
		boolean last1 = last.from.equals(s.getName()) && last.when.after(new Date(System.currentTimeMillis() - (10 * 60 * 1000)));

		last = splits[splits.length - 2];
		boolean last2 = last.from.equals(s.getName()) && last.when.after(new Date(System.currentTimeMillis() - (20 * 60 * 1000)));

		return last1 && last2;
	}

	public Server findPreferred()
	{
		if (this.from.getSplit() != null && findValidReconnectorFor(this.from) != null)
			return this.serv; // Special case.

		for (Iterator<String> it = this.serv.allowed_clines.iterator(); it.hasNext();)
		{
			final String pname = it.next();
			Server pserver = Server.findServerAbsolute(pname);

			if (isGood(pserver))
				return pserver;
		}

		if (isGood(this.from))
			return this.from;

		if (!this.serv.allowed_clines.isEmpty())
			return null;

		// Take a guess. Highest hub is probably good.
		Server highest = null;
		for (Iterator<String> it = this.serv.clines.iterator(); it.hasNext();)
		{
			Server altserver = Server.findServerAbsolute(it.next());
			if (isGood(altserver) && altserver.isHub()
					&& (highest == null || altserver.links.size() > highest.links.size()))
				highest = altserver;
		}

		return highest;
	}

	@Override
	public void run()
	{
		if (last_reconnect + 60 > System.currentTimeMillis() / 1000L)
			return;

		++this.tick;

		Server s = Server.findServerAbsolute(this.serv.getName());
		if (s == null || this.sp == null || !this.sp.equals(s.getSplit()))
		{
			this.destroy();
			return;
		}

		if (!servermonitor.conf.reconnect)
		{
			Moo.privmsgAll(Moo.conf.split_channels, "Disabling reconnect for frozen server " + s.getName());

			this.destroy();
			return;
		}

		Server targ = this.findPreferred();
		if (targ == this.serv) // Special case, hold due to the split probably being between me and serv
		{
			Moo.privmsgAll(Moo.conf.split_channels, "Delaying reconnect for " + this.serv.getName() + " due to its uplink being split");
			return;
		}

		if (this.tries == 7 || targ == null)
		{
			for (final String chan : Moo.conf.split_channels)
			{
				if (targ == null)
				{
					List<String> others = new ArrayList<String>();
					others.addAll(s.clines);

					// remove attempted
					for (String s2 : attempted)
						others.remove(s2);

					// get suggestion
					Server guess = null;
					for (String s2 : others)
					{
						Server tmp = Server.findServerAbsolute(s2);
						if (tmp != null && isGood(tmp) && tmp.isHub() && (guess == null || tmp.links.size() > guess.links.size()))
							guess = tmp;
					}

					String buf = "I am no longer able to intelligently route " + s.getName() + ". Other servers may be: " + others + ".";
					if (guess != null)
						buf += " My best guess is " + guess.getName() + ".";

					if (!others.isEmpty())
						Moo.privmsg(chan, buf);
				}
				Moo.privmsg(chan, "Giving up reconnecting " + s.getName() + ", tried " + this.tries + " times in " + this.tick + " minutes to " + this.attempted.size() + " servers: " + this.attempted.toString());
			}

			this.destroy();

			return;
		}

		int delay = this.serv.isHub() && this.tries == 0 ? 3 : 2;

		if (this.tick % delay != 0)
		{
			int wait = 0;
			for (int i = this.tick; i % delay != 0; ++wait, ++i);
			Moo.privmsgAll(Moo.conf.split_channels, "Will reconnect " + s.getName() + " to " + targ.getName() + " in " + wait + " minute" + (wait != 1 ? "s" : ""));
			return;
		}

		++this.tries;

		Moo.privmsgAll(Moo.conf.split_channels, "Reconnect #" + this.tries + " for " + s.getName() + " to " + targ.getName());

		Moo.write("CONNECT", s.getName(), servermonitor.conf.port, targ.getName());
		this.sp.reconnectedBy = Moo.conf.general.nick;

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
		for (Reconnector r : new ArrayList<Reconnector>(reconnects))
		{
			if (r.serv == s)
			{
				r.destroy();
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
