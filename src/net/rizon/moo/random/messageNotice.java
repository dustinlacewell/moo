package net.rizon.moo.random;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.message;
import net.rizon.moo.server;

class messageNotice extends message
{
	private static final Pattern connectPattern = Pattern.compile(".* Client connecting.*: ([^ ]*) \\(~?([^@]*).*?\\) \\[([0-9.]*)\\] \\[(.*)\\]");
	
	public messageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (source.indexOf('.') == -1)
			return;
		
		Date then = new Date(System.currentTimeMillis() - (30 * 1000)); // 30 seconds ago
		if (server.last_link != null && server.last_link.after(then))
			return;
		else if (server.last_split != null && server.last_split.after(then))
			return;
		
		Matcher m = connectPattern.matcher(message[1]);
		if (m.matches())
		{
			final String nick = m.group(1), ident = m.group(2), ip = m.group(3), real = m.group(4);
			
			if (ident.equals("cgiirc") || real.equals("http://www.mibbit.com"))
				return;
			
			nickData nd = new nickData(nick, ident, real, ip);
			random.addNickData(nd);
		}
	}
}