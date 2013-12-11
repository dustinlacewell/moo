package net.rizon.moo.protocol.unreal;

import net.rizon.moo.Message;
import net.rizon.moo.Server;

/* End of map */
class Message007 extends Message
{
	public Message007()
	{
		super("007");
	}

	@Override
	public void run(String source, String[] message)
	{
		Server.last_total_users = Server.cur_total_users;
		Server.cur_total_users = Server.work_total_users;
		Server.work_total_users = 0;
	}
}