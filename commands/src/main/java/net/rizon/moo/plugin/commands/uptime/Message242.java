package net.rizon.moo.plugin.commands.uptime;

import com.google.inject.Inject;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import org.slf4j.Logger;

public class Message242 extends Message
{
	@Inject
	private static Logger logger;
	
	@Inject
	private ServerManager serverManager;

	@Inject
	private CommandUptime commandUptime;
	
	protected static CommandSource source;
	public static Set<String> waiting_for = new HashSet<>();

	public Message242()
	{
		super("242");
	}

	@Override
	public void run(IRCMessage message)
	{
		Server s = serverManager.findServerAbsolute(message.getSource());
		if (s == null)
		{
			s = new Server(message.getSource());
			serverManager.insertServer(s);
		}

		String upstr = message.getParams()[1];
		String[] tokens = upstr.split(" ");
		String[] times = tokens[4].split(":");

		int days, hours, mins, secs;
		try
		{
			days = Integer.parseInt(tokens[2]);
			hours = Integer.parseInt(times[0]);
			mins = Integer.parseInt(times[1]);
			secs = Integer.parseInt(times[2]);
		}
		catch (NumberFormatException ex)
		{
			logger.warn("Unable to parse 242", ex);
			return;
		}

		long total_ago = secs + (mins * 60) + (hours * 60 * 60) + (days * 60 * 60 * 24 );
		s.uptime = new Date(System.currentTimeMillis() - (total_ago * 1000L));

		waiting_for.remove(s.getName());

		if (waiting_for.isEmpty())
		{
			commandUptime.post_update(Message242.source);
		}
	}
}
