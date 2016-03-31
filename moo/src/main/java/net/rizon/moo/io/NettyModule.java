package net.rizon.moo.io;

import com.google.inject.AbstractModule;

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
		bind(InboundLoggingHandler.class);
		bind(OutboundLoggingHandler.class);
		bind(ClientHandler.class).toInstance(handler);
	}
}
