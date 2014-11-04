package net.rizon.moo.protocol;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.User;

public class Message001 extends Message
{
	public Message001()
	{
		super("001");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (Moo.conf.general.oper != null)
			Moo.sock.write("OPER " + Moo.conf.general.oper.name + " " + Moo.conf.general.oper.pass);
		if (Moo.conf.general.nickserv != null)
			Moo.privmsg("NickServ", "IDENTIFY " + Moo.conf.general.nickserv.pass);

		Moo.me = new User(Moo.conf.general.nick);
		Moo.users.add(Moo.me);

		for (String s : Moo.conf.channels)
			Moo.join(s);
		
		Moo.sock.write("MAP");
		Moo.sock.write("LINKS");
		
		for (Server s : Server.getServers())
		{
			if (s.isServices())
				continue;
			
			s.requestStats();
			Moo.sock.write("VERSION " + s.getName());
		}
		
		for (Event e : Event.getEvents())
			e.onConnect();
	}
}
