package net.rizon.moo.commits;

import net.rizon.moo.event;

class eventCommit extends event
{
	@Override
	public void saveDatabases()
	{
		commits.s.shutdown();
	}
}