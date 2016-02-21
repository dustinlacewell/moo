package net.rizon.moo.plugin.commands.why;

import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;

public class Message219Why extends Message
{
	public Message219Why()
	{
		super("219");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (CommandWhy.host_ip.isEmpty())
			return;

		CommandWhy.requested--;

		if (CommandWhy.requested == 0)
		{
			CommandWhy.command_source.reply(CommandWhy.host_ip + " (" + CommandWhy.host_host + ") is not banned");

			CommandWhy.host_ip = "";
			CommandWhy.host_host = "";
		}
	}
}
