package net.rizon.moo.plugin.servermonitor;

import com.google.inject.Inject;
import java.util.LinkedList;
import net.rizon.moo.Mail;
import net.rizon.moo.plugin.servermonitor.conf.ServerMonitorConfiguration;

class TextDelay implements Runnable
{
	enum LinkType
	{
		SPLIT,
		RELINK,
		UNKNOWN
	}

	public static final int delay = 5;
	
	@Inject
	private Mail mail;
	
	@Inject
	private ServerMonitorConfiguration conf;
	
	@Inject
	private servermonitor servermonitor;

	@Override
	public void run()
	{
		String buf = "";
		for (String s : messages)
		{
			if (!buf.isEmpty())
				buf += " / ";
			buf += s;
		}

		for (String email : conf.split_emails)
		{
			switch (this.type)
			{
				case SPLIT:
					mail.send(email, "Split", buf);
					servermonitor.splitTexts = null;
					break;
				case RELINK:
					mail.send(email, "Relink", buf);
					servermonitor.linkTexts = null;
					break;
				default:
					break;
			}
		}
	}

	protected LinkedList<String> messages = new LinkedList<String>();
	protected LinkType type = LinkType.UNKNOWN;
}
