package net.rizon.moo.plugin.commits;

import net.rizon.moo.Event;

class EventCommit extends Event
{
	@Override
	public void onShutdown()
	{
		commits.s.shutdown();
	}
}