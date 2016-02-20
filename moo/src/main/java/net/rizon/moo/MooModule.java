package net.rizon.moo;

import net.rizon.moo.events.EventManager;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import net.rizon.moo.injectors.logger.LogTypeListener;

public class MooModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(EventBus.class);
		
		bind(CommandManager.class);
		bind(MessageManager.class);
		bind(EventManager.class);
		
		bindListener(Matchers.any(), new LogTypeListener());
	}

	@Provides
	io.netty.channel.Channel provideNettyChannel(Moo moo)
	{
		return moo.channel;
	}
	
	@Provides
	EventBus provideEventBus(EventManager eventMangager)
	{
		return eventMangager.getEventBus();
	}
}
