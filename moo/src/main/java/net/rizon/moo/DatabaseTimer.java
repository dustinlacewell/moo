package net.rizon.moo;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import net.rizon.moo.events.SaveDatabases;

public class DatabaseTimer implements Runnable
{
	@Inject
	private EventBus eventBus;

	@Override
	public void run()
	{
		eventBus.post(new SaveDatabases());
	}
}
