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
		if (moo.conf.getOper() != null && moo.conf.getOper().isEmpty() == false)
			moo.sock.write("OPER " + moo.conf.getOper());
		if (moo.conf.getGeoServPass() != null && moo.conf.getGeoServPass().isEmpty() == false)
			moo.sock.privmsg("GeoServ", "ACCESS IDENTIFY " + moo.conf.getGeoServPass());
		
		if (moo.conf.getChannels() != null && moo.conf.getChannels().length > 0)
			for (int i = 0; i < moo.conf.getChannels().length; ++i)
				moo.sock.join(moo.conf.getChannels()[i]);
		
		server.clearServers();
		moo.sock.write("MAP");
	}
}