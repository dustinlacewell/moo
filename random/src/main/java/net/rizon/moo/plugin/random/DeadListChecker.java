package net.rizon.moo.plugin.random;

import com.google.inject.Inject;
import java.util.Iterator;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;

class DeadListChecker implements Runnable
{
	@Inject
	private Protocol protocol;

	@Inject
	private Config conf;

	@Override
	public void run()
	{
		long now_l = System.currentTimeMillis() / 1000L;

		for (Iterator<FloodList> it = FloodList.getLists().iterator(); it.hasNext();)
		{
			FloodList p = it.next();

			if (p.isClosed)
				continue;

			if (p.getMatches().isEmpty() || now_l - p.getTimes().getFirst() > random.timeforMatches)
			{
				if (p.isList)
				{
					protocol.privmsgAll(conf.flood_channels, "[FLOOD] End of flood for " + p.toString() + " - " + p.getMatches().size() + " matches");

					/* Don't really close this, we want the list to persist forever. */
					p.isClosed = true;
				}
				else
				{
					/* List hasn't been touched in awhile, delete it */
					it.remove();
					p.close();
				}
			}
		}
	}
}
