package net.rizon.moo.messages;

import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class message001 extends message
{
	public message001()
	{
		super("001");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (moo.conf.getOper().isEmpty() == false)
			moo.sock.write("OPER " + moo.conf.getOper());
		if (moo.conf.getNickServPass().isEmpty() == false)
			moo.sock.privmsg("NickServ", "IDENTIFY " + moo.conf.getNickServPass());
		if (moo.conf.getGeoServPass().isEmpty() == false)
			moo.sock.privmsg("GeoServ", "ACCESS IDENTIFY " + moo.conf.getGeoServPass());
		
		for (int i = 0; i < moo.conf.getChannels().length; ++i)
			moo.sock.join(moo.conf.getChannels()[i]);
		for (int i = 0; i < moo.conf.getIdleChannels().length; ++i)
			moo.sock.join(moo.conf.getIdleChannels()[i]);
		
		server.clearServers();

		moo.sock.write("MAP");
		moo.sock.write("LINKS"); // This returns numeric 365, we load databases here
	}
}