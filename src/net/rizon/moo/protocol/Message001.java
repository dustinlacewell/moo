package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;

public class Message001 extends Message
{
	public Message001()
	{
		super("001");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (Moo.conf.getOper().isEmpty() == false)
			Moo.sock.write("OPER " + Moo.conf.getOper());
		if (Moo.conf.getNickServPass().isEmpty() == false)
			Moo.privmsg("NickServ", "IDENTIFY " + Moo.conf.getNickServPass());
		
		for (int i = 0; i < Moo.conf.getChannels().length; ++i)
			Moo.join(Moo.conf.getChannels()[i]);
		for (int i = 0; i < Moo.conf.getIdleChannels().length; ++i)
			Moo.join(Moo.conf.getIdleChannels()[i]);
		
		Server.clearServers();

		Moo.sock.write("MAP");
		Moo.sock.write("LINKS"); // This returns numeric 365, we load databases here
		
		for (Event e : Event.getEvents())
			e.onConnect();
	}
}
