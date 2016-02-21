package net.rizon.moo.plugin.commands.why;

import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;

public class Message225 extends Message
{
	public Message225()
	{
		super("225");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams()[1].equals("d") == false)
			return;

		if (CommandWhy.host_ip.isEmpty())
			return;

		if (message.getParams()[2].equalsIgnoreCase(CommandWhy.host_ip) == false && message.getParams()[2].equalsIgnoreCase(CommandWhy.host_host) == false)
			return;

		CommandWhy.command_source.reply("[" + message.getSource() + "] " + message.getParams()[2] + " is " + message.getParams()[1] + "-lined for: " + message.getParams()[3]);

		CommandWhy.host_ip = "";
		CommandWhy.host_host = "";
	}
}