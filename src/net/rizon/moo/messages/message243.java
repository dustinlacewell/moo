package net.rizon.moo.messages;

import net.rizon.moo.message;
import net.rizon.moo.server;

public class message243 extends message
{
	public message243()
	{
		super("243");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 5)
			return;
		
		String oper = message[4];
		
		server s = server.findServerAbsolute(source);
		if (s == null)
			s = new server(source);
		if (s.isServices())
			return;
		s.olines.add(oper);
	}
}