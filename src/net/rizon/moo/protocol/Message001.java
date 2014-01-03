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
		if (Moo.conf.getString("oper").isEmpty() == false)
			Moo.sock.write("OPER " + Moo.conf.getString("oper"));
		if (Moo.conf.getString("nickserv_pass").isEmpty() == false)
			Moo.privmsg("NickServ", "IDENTIFY " + Moo.conf.getString("nickserv_pass"));
		
		for (String s : Moo.conf.getList("channels"))
			Moo.join(s);
		
		Server.clearServers();

		Moo.sock.write("MAP");
		Moo.sock.write("LINKS"); // This returns numeric 365, we load databases here
		
		for (Event e : Event.getEvents())
			e.onConnect();
	}
}
