package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.Server;

/* End of map */
class Message017 extends Message
{
	public Message017()
	{
		super("017");
	}

	@Override
	public void run(String source, String[] message)
	{
		Server.last_total_users = Server.cur_total_users;
		Server.cur_total_users = Server.work_total_users;
		Server.work_total_users = 0;
	}
}