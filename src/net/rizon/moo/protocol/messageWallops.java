package net.rizon.moo.protocol;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.event;
import net.rizon.moo.message;

public class messageWallops extends message
{
	private static final Pattern akillAddPattern = Pattern.compile(".* ([^ ]*) added an AKILL for \\*@([0-9A-Fa-f:.]*) \\(([^)]*)\\).*"),
			akillRemovePattern = Pattern.compile(".* ([^ ]*) removed an AKILL for \\*@([0-9A-Fa-f:.]*) \\(([^)]*)\\).*");
	
	public messageWallops()
	{
		super("WALLOPS");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 1)
			return;
		
		for (event e : event.getEvents())
			e.onWallops(source, message[0]);
		
		Matcher m = akillAddPattern.matcher(message[0]);
		if (m.matches())
		{
			final String setter = m.group(1), ip = m.group(2), reason = m.group(3);
			for (event e : event.getEvents())
				e.onAkillAdd(setter, ip, reason);
			return;
		}
		
		m = akillRemovePattern.matcher(message[0]);
		if (m.matches())
		{
			final String setter = m.group(1), ip = m.group(2), reason = m.group(3);
			for (event e : event.getEvents())
				e.onAkillDel(setter, ip, reason);;
			return;
		}
	}
}
