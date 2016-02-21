package net.rizon.moo.plugin.commands.slackers;

import com.google.inject.Inject;
import java.util.Iterator;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Channel;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.User;

public class Message219Slackers extends Message
{
	@Inject
	private IRC irc;
	
	public Message219Slackers()
	{
		super("219");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams()[1].equals("p") == false)
			return;

		CommandSlackers.waiting_on.remove(message.getSource());
		if (CommandSlackers.waiting_on.isEmpty() && CommandSlackers.command_source != null)
		{
			Channel c = irc.findChannel(CommandSlackers.command_source.getTargetName());

			for (Iterator<String> it = CommandSlackers.opers.iterator(); it.hasNext(); )
			{
				String user = it.next();
				User u = irc.findUser(user);

				if (c != null && u != null && c.findUser(u) != null)
					it.remove();
			}

			if (CommandSlackers.opers.isEmpty())
				CommandSlackers.command_source.reply("There are no opers missing from " + CommandSlackers.command_source.getTargetName());
			else
			{
				CommandSlackers.command_source.reply("There are " + CommandSlackers.opers.size() + " opers missing from " + CommandSlackers.command_source.getTargetName() + ":");
				String operbuf = "";
				for (int i = 0; i < CommandSlackers.opers.size(); ++i)
				{
					operbuf += " " + CommandSlackers.opers.get(i);
					if (operbuf.length() > 200)
					{
						CommandSlackers.command_source.reply(operbuf.substring(1));
						operbuf = "";
					}
				}
				if (operbuf.isEmpty() == false)
					CommandSlackers.command_source.reply(operbuf.substring(1));
			}

			CommandSlackers.opers.clear();
			CommandSlackers.command_source = null;
			CommandSlackers.waiting_on.clear();
		}
	}
}
