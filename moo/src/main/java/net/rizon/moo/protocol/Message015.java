package net.rizon.moo.protocol;


import com.google.inject.Inject;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import org.slf4j.Logger;

/* Map
 * <- :adam.home 015 Adam :adam.home[00C] ----------------------------------- | Users:     1 (100.0%)
 */
class Message015 extends Message
{
	@Inject
	private static Logger logger;

	public Message015()
	{
		super("015");
	}

	private static boolean isValidServerChar(char c)
	{
		return c == '.' || c == '-' || (c >= '0' && c <= '9') || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z');
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams().length < 2)
			return;

		String map = message.getParams()[1];

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
				logger.warn("Invalid user count in map 015: {}", s);
			}
		}

		Server serv = Server.findServerAbsolute(name);
		if (serv == null)
			serv = new Server(name);
		serv.setSID(sid);
		serv.last_users = serv.users;
		serv.users = users;
		Server.work_total_users += users;
	}
}
