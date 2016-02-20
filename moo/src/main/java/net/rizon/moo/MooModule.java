package net.rizon.moo;

import net.rizon.moo.events.EventManager;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.injectors.logger.LogTypeListener;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.ServerManager;

public class MooModule extends AbstractModule
{
	private static IRC irc = new IRC();

	@Override
	protected void configure()
	{
		bind(EventBus.class).toInstance(new EventBus());
		
		bind(CommandManager.class).toInstance(new CommandManager());
		bind(MessageManager.class).toInstance(new MessageManager());
		bind(EventManager.class).toInstance(new EventManager());
		bind(ServerManager.class).toInstance(new ServerManager());
		bind(DatabaseTimer.class).toInstance(new DatabaseTimer());
		
		bindListener(Matchers.any(), new LogTypeListener());

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().to(ServerManager.class);
	}

	@Provides
	IRC provideIRC()
	{
		return irc;
	}
}
