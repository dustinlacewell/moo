package net.rizon.moo.protocol;

import net.rizon.moo.message;
import net.rizon.moo.server;

/* /stats o */
public class message243 extends message
{
	public message243()
	{
		super("243");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 6)
			return;
		
		String oper = message[4];
		
		server s = server.findServerAbsolute(source);
		if (s == null)
			s = new server(source);
		s.olines_work.put(oper, message[5]);
	}
}