package net.rizon.moo.watch;

import java.util.Date;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.message;
import net.rizon.moo.moo;

public class messagePrivmsg extends message
{
	private static final Pattern bopm_pattern = Pattern.compile("^BOPM: Banned (.*?)!");
	private static final long ban_time = 86400 * 3 * 1000L; // 3d

	public messagePrivmsg()
	{
		super("PRIVMSG");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (moo.conf.isIdleChannel(message[0]))
			return;

		Matcher m = bopm_pattern.matcher(message[1]);
		if (m.find())
		{
			final String nick = m.group(1);
			
			for (Iterator<watchEntry> it = watch.watches.iterator(); it.hasNext();)
			{
				watchEntry e = it.next();
				
				if (e.nick.equalsIgnoreCase(nick))
					return;
			}

			watchEntry we = new watchEntry();
			we.nick = nick;
			we.creator = moo.conf.getNick();
			we.reason = "Suspected open proxy";
			we.created = new Date();
			we.expires = new Date(System.currentTimeMillis() + ban_time);
			we.registered = watchEntry.registeredState.RS_UNKNOWN;
				
			watch.watches.add(we);
		}
	}
}
