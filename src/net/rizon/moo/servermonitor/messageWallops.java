package net.rizon.moo.servermonitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.message;
import net.rizon.moo.server;
import net.rizon.moo.split;

class messageWallops extends message
{
	private static final Pattern connectPattern = Pattern.compile("Remote CONNECT ([^ ]*) [0-9]* from ([^ ]*)$");
	
	public messageWallops()
	{
		super("WALLOPS");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (source.indexOf('.') == -1 || message.length < 1)
			return;
		
		Matcher m = connectPattern.matcher(message[0]);
		if (m.find())
		{
			server s = server.findServer(m.group(1));
			if (s == null)
				return;
			
			split sp = s.getSplit();
			if (sp == null)
				return;
			
			sp.reconnectedBy = m.group(2);
		}
	}
}