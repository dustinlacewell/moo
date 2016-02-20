package net.rizon.moo;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;
import net.rizon.moo.injectors.logger.LogTypeListener;

public class MooModule extends AbstractModule
{
	@Override
	protected void configure()
	{
		bind(CommandManager.class);
		bind(MessageManager.class);
		//bindListener(Matchers.annotatedWith(Inject.class), new LogTypeListener());
		bindListener(Matchers.any(), new LogTypeListener());
	}

}
