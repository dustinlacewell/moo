package net.rizon.moo.protocol.plexus;

import net.rizon.moo.message;
import net.rizon.moo.server;

/* End of map */
class message017 extends message
{
	public message017()
	{
		super("017");
	}

	@Override
	public void run(String source, String[] message)
	{
		server.last_total_users = server.cur_total_users;
		server.cur_total_users = server.work_total_users;
		server.work_total_users = 0;
	}
}