package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventAkillAdd;
import net.rizon.moo.events.EventAkillDel;
import net.rizon.moo.events.EventWallops;

public class MessageWallops extends Message
{
	private static final Pattern akillAddPattern = Pattern.compile(".* ([^ ]*) added an AKILL for \\*@([0-9A-Fa-f:.]*) \\(([^)]*)\\).*"),
			akillRemovePattern = Pattern.compile(".* ([^ ]*) removed an AKILL for \\*@([0-9A-Fa-f:.]*) \\(([^)]*)\\).*");
	
	@Inject
	private EventBus eventBus;

	public MessageWallops()
	{
		super("WALLOPS");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 1)
			return;

		eventBus.post(new EventWallops(source, message[0]));

		Matcher m = akillAddPattern.matcher(message[0]);
		if (m.matches())
		{
			String setter = m.group(1), ip = m.group(2), reason = m.group(3);
			eventBus.post(new EventAkillAdd(setter, ip, reason));
			return;
		}

		m = akillRemovePattern.matcher(message[0]);
		if (m.matches())
		{
			String setter = m.group(1), ip = m.group(2), reason = m.group(3);
			eventBus.post(new EventAkillDel(setter, ip, reason));
			return;
		}
	}
}
