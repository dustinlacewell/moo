package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.Date;
import net.rizon.moo.CommandManager;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventPrivmsg;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Protocol;

public class MessagePrivmsg extends Message
{
	@Inject
	private Protocol protocol;
	
	@Inject
	private EventBus eventBus;
	
	@Inject
	private CommandManager manager;

	@Inject
	private Config conf;
	
	public MessagePrivmsg()
	{
		super("PRIVMSG");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 2)
			return;

		String target = message.getParams()[0], text = message.getParams()[1];

		eventBus.post(new EventPrivmsg(message.getSource(), target, text));

		if (text.equals("\1VERSION\1"))
			protocol.notice(message.getNick(), "\1VERSION " + conf.version + "\1");
		else if (text.equals("\1TIME\1"))
			protocol.notice(message.getNick(), "\1TIME " + (new Date().toString()) + "\1");
		
		manager.run(message);
	}
}