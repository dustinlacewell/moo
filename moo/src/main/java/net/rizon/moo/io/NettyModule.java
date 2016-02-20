package net.rizon.moo.io;

import com.google.inject.AbstractModule;
import io.netty.channel.Channel;

public class NettyModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(ClientInitializer.class);
		bind(Handler.class);
		bind(LoggingHandler.class);
		bind(ClientHandler.class);
		bind(Channel.class).toProvider(ChannelProvider.class);
	}
}
