package net.rizon.moo.protocol;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.Date;
import net.rizon.moo.CommandManager;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.events.EventPrivmsg;
import net.rizon.moo.irc.Protocol;

public class MessagePrivmsg extends Message
{
	@Inject
	private Protocol protocol;
	
	@Inject
	private EventBus eventBus;
	
	@Inject
	private CommandManager manager;
	
	public MessagePrivmsg()
	{
		super("PRIVMSG");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;

		eventBus.post(new EventPrivmsg(source, message[0], message[1]));

		if (message[1].equals("\1VERSION\1"))
			protocol.notice(source, "\1VERSION " + Moo.conf.version + "\1");
		else if (message[1].equals("\1TIME\1"))
			protocol.notice(source, "\1TIME " + (new Date().toString()) + "\1");
		
		manager.run(source, message);
	}
}