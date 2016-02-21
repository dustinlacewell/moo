package net.rizon.moo.plugin.fun;

import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.util.Arrays;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;

public class fun extends Plugin implements EventListener
{
	@Inject
	private CommandRizonTime rizonTime;

	public fun() throws Exception
	{
		super("FUN", "Provides fun features");
	}

	@Override
	public void start() throws Exception
	{
	}

	@Override
	public void stop()
	{
	}

	@Override
	protected void configure()
	{
		bind(fun.class).toInstance(this);
		
		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandRizonTime.class);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);
		eventListenerBinder.addBinding().to(EventFun.class);
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(rizonTime);
	}
}