package net.rizon.moo.plugin.servermonitor;

import java.util.LinkedList;
import net.rizon.moo.Mail;

class TextDelay implements Runnable
{
	public static final int delay = 5;

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

		for (String email : servermonitor.conf.split_emails)
			Mail.send(email, "Split", buf);

		servermonitor.texts = null;
	}

	protected LinkedList<String> messages = new LinkedList<String>();
}