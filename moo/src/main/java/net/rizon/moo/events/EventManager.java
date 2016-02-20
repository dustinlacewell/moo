package net.rizon.moo.events;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.Set;

public class EventManager
{
	@Inject
	private EventBus bus;

	@Inject
	private Set<EventListener> eventListeners;

	public void build()
	{
		for (EventListener l : eventListeners)
			bus.register(bus);
	}
}
