package net.rizon.moo.protocol.unreal;

import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Message;
import net.rizon.moo.Server;

/* Map
<- :adam.home 006 Adam :adam.home  [Users: 1] [100.0%] 1
 */
class Message006 extends Message
{
	private static final Logger log = Logger.getLogger(Message006.class.getName());
	
	public Message006()
	{
		super("006");
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
		
		int users = -1;
		i = map.indexOf("Users:");
		if (i != -1)
		{
			String s = map.substring(i + 6);
			s = s.trim();
			i = s.indexOf(' ');
			if (i > 0)
				s = s.substring(0, i - 1);
			try
			{
				users = Integer.parseInt(s);
			}
			catch (NumberFormatException ex)
			{
				log.log(Level.WARNING, "Invalid user count in map 015: " + s);
			}
		}
		
		Server serv = Server.findServerAbsolute(name);
		if (serv == null)
			serv = new Server(name);
		serv.last_users = serv.users;
		serv.users = users;
		Server.work_total_users += users;
	}
}
