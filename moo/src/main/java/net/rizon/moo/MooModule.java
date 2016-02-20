package net.rizon.moo;

import net.rizon.moo.events.EventManager;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import net.rizon.moo.injectors.logger.LogTypeListener;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.ServerManager;

public class MooModule extends AbstractModule
{
	private static IRC irc = new IRC();

	@Override
	protected void configure()
	{
		bind(EventBus.class);
		
		bind(CommandManager.class);
		bind(MessageManager.class);
		bind(EventManager.class);
		bind(ServerManager.class);
		bind(DatabaseTimer.class);
		
		bindListener(Matchers.any(), new LogTypeListener());
	}

	@Provides
	IRC provideIRC()
	{
		return irc;
	}
}
