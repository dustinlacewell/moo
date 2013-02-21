package net.rizon.moo.protocol.plexus;

import net.rizon.moo.message;
import net.rizon.moo.server;

/* Map
 * <- :adam.home 015 Adam :adam.home[00C] ----------------------------------- | Users:     1 (100.0%)
 */
class message015 extends message
{
	public message015()
	{
		super("015");
	}
	
	private static boolean isValidServerChar(char c)
	{
		return c == '.' || c == '-' || (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z'); 
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
		for (; i < map.length() && isValidServerChar(map.charAt(i)); ++i)
			name += map.charAt(i);
		
		String sid = "";
		++i;
		for (int j = 0; i < map.length() && j < 3; ++i, ++j)
			sid += map.charAt(i);
		
		int users = -1;
		i = map.indexOf("Users:");
		if (i != -1)
		{
			String s = map.substring(i + 6);
			s = s.trim();
			i = s.indexOf(' ');
			if (i != -1)
				s = s.substring(0, i);
			try
			{
				users = Integer.parseInt(s);
			}
			catch (NumberFormatException ex)
			{
				System.err.println("Invalid user count in map 015: " + s);
			}
		}
		
		server serv = server.findServerAbsolute(name);
		if (serv == null)
			serv = new server(name);
		serv.setSID(sid);
		serv.last_users = serv.users;
		serv.users = users;
		server.work_total_users += users;
	}
}
