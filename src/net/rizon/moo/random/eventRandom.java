package net.rizon.moo.random;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.event;
import net.rizon.moo.server;

class eventRandom extends event
{
	private static final Pattern connectPattern = Pattern.compile(".* Client connecting.*: ([^ ]*) \\(~?([^@]*).*?\\) \\[([0-9.]*)\\] \\[(.*)\\]");

	@Override
	public void onNotice(final String source, final String channel, final String message)
	{
		if (source.indexOf('.') == -1)
			return;
		
		Date then = new Date(System.currentTimeMillis() - (30 * 1000)); // 30 seconds ago
		if (server.last_link != null && server.last_link.after(then))
			return;
		else if (server.last_split != null && server.last_split.after(then))
			return;
		
		Matcher m = connectPattern.matcher(message);
		if (m.matches())
		{
			final String nick = m.group(1), ident = m.group(2), ip = m.group(3), real = m.group(4);
			
			if (ident.equals("qwebirc") || ident.equals("cgiirc") || real.equals("http://www.mibbit.com") || real.equals("..."))
				return;
			
			nickData nd = new nickData(nick, ident, real, ip);
			random.addNickData(nd);
		}
	}
}