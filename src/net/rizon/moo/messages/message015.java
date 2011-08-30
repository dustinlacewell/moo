package net.rizon.moo.messages;

import net.rizon.moo.message;
import net.rizon.moo.server;

public class message015 extends message
{
	public message015()
	{
		super("015");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;
		
		String map = message[1];
		
		int i = 0;
		for (; i < map.length(); ++i)
			if (Character.isLetter(map.charAt(i)) == true)
				break;
		
		String name = "";
		for (; i < map.length() && (map.charAt(i) == '.' || Character.isLetter(map.charAt(i))); ++i)
			name += map.charAt(i);
		
		server serv = server.findServerAbsolute(name);
		if (serv == null)
			serv = new server(name);
		else
			serv.splitDel();
	}
}