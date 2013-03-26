package net.rizon.moo.random;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.event;
import net.rizon.moo.moo;
import net.rizon.moo.server;

class eventRandom extends event
{
	private static final Pattern connectPattern = Pattern.compile(".* Client connecting.*: ([^ ]*) \\(~?([^@]*).*?\\) \\[([0-9.]*)\\] \\[(.*)\\]"),
			akillAddPattern = Pattern.compile("[^ ]* added an AKILL for \\*@([0-9A-Fa-f:.]*) "),
			akillRemovePattern = Pattern.compile("[^ ]* removed an AKILL for \\*@([0-9A-Fa-f:.]*) ");
	
	@Override
	protected void initDatabases()
	{
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `akills` (`date` DATE DEFAULT CURRENT_TIMESTAMP, `ip`, `count`)");
		moo.db.executeUpdate("CREATE UNIQUE INDEX IF NOT EXISTS `akills_ip_idx` on `akills` (`ip`)");
	}
	
	@Override
	public void onWallops(final String source, final String message)
	{
		Matcher m = akillAddPattern.matcher(message);
		if (m.matches())
		{
			if (message.contains("hopm") || message.contains("open proxies") || message.contains("open proxy"))
				return;
			
			final String ip = m.group(1);
			random.akill(ip);
			return;
		}
		
		m = akillRemovePattern.matcher(message);
		if (m.matches())
		{
			final String ip = m.group(1);
			if (random.remove(ip))
				for (int i = 0; i < moo.conf.getFloodChannels().length; ++i)
					moo.privmsg(moo.conf.getFloodChannels()[i], "Removed IP " + ip + " from akill history.");
			return;
		}
	}

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
			
			if (ident.equals("qwebirc") || ident.equals("cgiirc") || real.equals("http://www.mibbit.com") || real.equals("...") || nick.startsWith("bRO-"))
				return;
			
			nickData nd = new nickData(nick, ident, real, ip);
			random.addNickData(nd);
		}
	}
}