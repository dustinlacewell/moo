package net.rizon.moo.io;

import com.google.inject.AbstractModule;

public class NettyModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(ClientInitializer.class);
		bind(Handler.class);
		bind(LoggingHandler.class);
		bind(ClientHandler.class);
	}
}
