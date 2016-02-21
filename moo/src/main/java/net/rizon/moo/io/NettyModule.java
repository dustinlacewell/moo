package net.rizon.moo.io;

import com.google.inject.AbstractModule;
import io.netty.channel.Channel;

public class NettyModule extends AbstractModule
{
	// Only allow one client handler, since netty calls back into this. We need to
	// update the injections into it
	private static final ClientHandler handler = new ClientHandler();

	@Override
	protected void configure()
	{
		bind(ClientInitializer.class);
		bind(Handler.class);
		bind(LoggingHandler.class);
		bind(ClientHandler.class).toInstance(handler);
		bind(Channel.class).toProvider(ChannelProvider.class);
	}
}
