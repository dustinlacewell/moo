package net.rizon.moo.protocol;

import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.ServerManager;

/* End of map */
class Message017 extends Message
{
	@Inject
	private ServerManager serverManager;

	public Message017()
	{
		super("017");
	}

	@Override
	public void run(IRCMessage message)
	{
		serverManager.last_total_users = serverManager.cur_total_users;
		serverManager.cur_total_users = serverManager.work_total_users;
		serverManager.work_total_users = 0;
	}
}