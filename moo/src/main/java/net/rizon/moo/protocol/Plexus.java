package net.rizon.moo.protocol;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import net.rizon.moo.Message;

public class Plexus extends AbstractModule
{
	@Override
	protected void configure()
	{
		Multibinder<Message> messageBinder = Multibinder.newSetBinder(binder(), Message.class);
		
		messageBinder.addBinding().to(Message001.class);
		messageBinder.addBinding().to(Message015.class);
		messageBinder.addBinding().to(Message017.class);
		messageBinder.addBinding().to(Message213.class);
		messageBinder.addBinding().to(Message219.class);
		messageBinder.addBinding().to(Message243.class);
		messageBinder.addBinding().to(Message303.class);
		messageBinder.addBinding().to(Message353.class);
		messageBinder.addBinding().to(Message364.class);
		messageBinder.addBinding().to(Message401.class);
		messageBinder.addBinding().to(Message474.class);
		messageBinder.addBinding().to(MessageInvite.class);
		messageBinder.addBinding().to(MessagePing.class);
		messageBinder.addBinding().to(MessagePrivmsg.class);
		messageBinder.addBinding().to(MessageJoin.class);
		messageBinder.addBinding().to(MessagePart.class);
		messageBinder.addBinding().to(MessageKick.class);
		messageBinder.addBinding().to(MessageMode.class);
		messageBinder.addBinding().to(MessageNick.class);
		messageBinder.addBinding().to(MessageNotice.class);
		messageBinder.addBinding().to(MessageQuit.class);
		messageBinder.addBinding().to(MessageWallops.class);
	}
}