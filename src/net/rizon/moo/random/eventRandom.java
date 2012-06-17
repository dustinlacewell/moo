package net.rizon.moo.random;

import net.rizon.moo.event;
import net.rizon.moo.moo;

public class eventRandom extends event
{
	@Override
	public void onConnect()
	{
		moo.privmsg("GeoServ", "CONNECTNICKS ADD " + moo.conf.getNick());
	}
}