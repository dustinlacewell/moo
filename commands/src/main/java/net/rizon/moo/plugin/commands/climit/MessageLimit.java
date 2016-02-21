package net.rizon.moo.plugin.commands.climit;

import com.google.inject.Inject;
import java.util.HashSet;
import java.util.Set;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

abstract class MessageLimit extends Message
{
	@Inject
	private ServerManager serverManager;

	private int dashesFor(Server s)
	{
		int longest = 0;
		for (Server s2 : serverManager.getServers())
		{
			int l = s2.getName().length();
			if (l > longest)
				longest = l;
		}

		return longest - s.getName().length() + 2;
	}

	public static Set<String> waiting_for = new HashSet<>();
	protected static CommandSource source;

	public MessageLimit(String what)
	{
		super(what);
	}

	@Override
	public void run(IRCMessage message)
	{
		Server s = serverManager.findServerAbsolute(message.getSource());
		if (s == null)
			return;

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < message.getParams().length - 1; ++i)
			sb.append(" " + message.getParams()[i]);

		String[] tokens = sb.toString().trim().split(" ");
		for (String token : tokens)
		{
			if (token.startsWith("CHANLIMIT="))
			{
				if (waiting_for.remove(s.getName()) == false)
					return;

				String limit = token.substring(12);
				String buf = "[CLIMIT] " + s.getName() + " ";
				for (int i = 0, dashes = dashesFor(s); i < dashes; ++i)
					buf += "-";
				buf += " \00309" + limit + "\003";

				source.reply(buf);
			}
		}
	}
}