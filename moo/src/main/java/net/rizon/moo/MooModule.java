package net.rizon.moo;

import net.rizon.moo.events.EventManager;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.injectors.logger.LogTypeListener;
import net.rizon.moo.irc.IRC;
import net.rizon.moo.irc.ServerManager;

public class MooModule extends AbstractModule
{
	private static IRC irc = new IRC();
	private static ServerManager serverManager = new ServerManager();
	private static PluginManager pluginManager = new PluginManager();

	private Moo moo;

	MooModule(Moo moo)
	{
		this.moo = moo;
	}

	@Override
	protected void configure()
	{
		bind(Moo.class).toInstance(moo);
		bind(Mail.class);
		
		bind(EventBus.class).toInstance(new EventBus());
		
		bind(CommandManager.class).toInstance(new CommandManager());
		bind(MessageManager.class).toInstance(new MessageManager());
		bind(EventManager.class).toInstance(new EventManager());
		bind(ServerManager.class).toInstance(serverManager);
		bind(DatabaseTimer.class).toInstance(new DatabaseTimer());
		bind(PluginManager.class).toInstance(pluginManager);
		bind(FutureExceptionListener.class);

		bindListener(Matchers.any(), new LogTypeListener());
		
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().to(ServerManager.class);
	}
	
	@Provides
	Config provideConfig()
	{
		return moo.getConf();
	}

	@Provides
	IRC provideIRC()
	{
		return irc;
	}
}
