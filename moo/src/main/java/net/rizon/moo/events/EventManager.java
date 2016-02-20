package net.rizon.moo.events;

import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import java.util.Set;

public class EventManager
{
	@Inject
	EventManager(EventBus bus, Set<EventListener> eventListeners)
	{
		for (EventListener l : eventListeners)
			bus.register(bus);
	}
}
