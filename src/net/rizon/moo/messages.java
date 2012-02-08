package net.rizon.moo;

import java.util.Date;
import java.util.HashSet;
import java.util.Random;

import net.rizon.moo.message;
import net.rizon.moo.moo;

class message001 extends message
{
	public message001()
	{
		super("001");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (moo.conf.getOper().isEmpty() == false)
			moo.sock.write("OPER " + moo.conf.getOper());
		if (moo.conf.getNickServPass().isEmpty() == false)
			moo.privmsg("NickServ", "IDENTIFY " + moo.conf.getNickServPass());
		if (moo.conf.getGeoServPass().isEmpty() == false)
			moo.privmsg("GeoServ", "ACCESS IDENTIFY " + moo.conf.getGeoServPass());
		
		for (int i = 0; i < moo.conf.getChannels().length; ++i)
			moo.join(moo.conf.getChannels()[i]);
		for (int i = 0; i < moo.conf.getIdleChannels().length; ++i)
			moo.join(moo.conf.getIdleChannels()[i]);
		
		server.clearServers();

		moo.sock.write("MAP");
		moo.sock.write("LINKS"); // This returns numeric 365, we load databases here
	}
}

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
		
		server serv = server.findServerAbsolute(name);
		if (serv == null)
			serv = new server(name);
		serv.setSID(sid);
	}
}

class message213 extends message
{
	public message213()
	{
		super("213");
	}

	@Override
	public void run(String source, String[] message)
	{
		server serv = server.findServerAbsolute(source);
		if (serv == null)
			serv = new server(source);
		
		if (message.length > 4)
			serv.clines.add(message[4]);
	}
}

class message227 extends message
{
	public message227()
	{
		super("227");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 4)
			return;
		
		final String name = message[2];
		long count;
		
		try
		{
			count = Long.parseLong(message[3]);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return;
		}
		
		server s = server.findServer(source);
		if (s == null)
			s = new server(source);

		s.dnsbl.put(name, count);
	}
}

class message243 extends message
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
		s.olines.add(oper);
	}
}

class message364 extends message
{
	public message364()
	{
		super("364");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 3)
			return;

		String from = message[1];
		String to = message[2];
		
		if (from.equals(to))
			return;
		
		server s = server.findServerAbsolute(from);
		if (s == null)
			s = new server(from);
		s.link(to);
		
		s = server.findServerAbsolute(to);
		if (s == null)
			s = new server(to);
		s.link(from);
	}
}

/* End of LINKS */
class message365 extends message
{
	public message365()
	{
		super("365");
	}

	@Override
	public void run(String source, String[] message)
	{
		for (event e : event.getEvents())
			e.loadDatabases();
	}
}

class message474 extends message
{
	public message474()
	{
		super("474");
	}
	
	private HashSet<String> invited = new HashSet<String>();

	@Override
	public void run(String source, String[] message)
	{
		if (this.invited.contains(message[1]))
		{
			this.invited.remove(message[1]);
			return;
		}
		else if (message.length > 1)
		{
			moo.privmsg("ChanServ", "UNBAN " + message[1]);
			moo.privmsg("ChanServ", "INVITE " + message[1]);
			moo.join(message[1]);
			this.invited.add(message[1]);
		}
	}
}

class messageInvite extends message
{
	public messageInvite()
	{
		super("INVITE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length > 1 && message[0].equalsIgnoreCase(moo.conf.getNick()))
		{
			for (int i = 0; i < moo.conf.getChannels().length; ++i)
				if (moo.conf.getChannels()[i].equalsIgnoreCase(message[1]))
				{
					moo.join(message[1]);
					break;
				}
			for (int i = 0; i < moo.conf.getIdleChannels().length; ++i)
				if (moo.conf.getIdleChannels()[i].equalsIgnoreCase(message[1]))
				{
					moo.join(message[1]);
					break;
				}
		}
	}
}

class messageNotice extends message
{
	public messageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (source.equals("NickServ!service@rizon.net"))
		{
			if (message.length > 1 && message[1].indexOf("This nickname is registered") != -1 && moo.conf.getNickServPass() != null && moo.conf.getNickServPass().isEmpty() == false)
				moo.privmsg(source, "IDENTIFY " + moo.conf.getNickServPass());
		}
		else if (source != null && source.indexOf('.') != -1 && message.length > 1)
		{
			if (message[1].indexOf("being introduced by") != -1)
			{
				String[] tokens = message[1].split(" ");
				
				server serv = server.findServerAbsolute(tokens[4]);
				if (serv == null)
				{
					serv = new server(tokens[4]);
					moo.sock.write("MAP");
				}
				else
					serv.splitDel(tokens[8]);
				serv.link(tokens[8]);
				
				server to = server.findServerAbsolute(tokens[8]);
				if (to == null)
					to = new server(tokens[8]);
				to.link(tokens[4]);
				
				for (event e : event.getEvents())
					e.onServerLink(serv, to);
			}
			else if (message[1].indexOf("End of burst from") != -1)
			{
				String[] tokens = message[1].split(" ");
				server serv = server.findServerAbsolute(tokens[7]);
				if (serv == null)
				{
					serv = new server(tokens[7]);
					moo.sock.write("MAP");
				}
				else
					serv.splitDel(source);
				serv.link(source);
				
				server to = server.findServerAbsolute(source);
				if (to == null)
					to = new server(source);
				to.link(tokens[7]);
				
				for (event e : event.getEvents())
					e.onServerLink(serv, to);
			}
			else if (message[1].indexOf("split from") != -1)
			{
				String[] tokens = message[1].split(" ");
				server from = server.findServerAbsolute(tokens[7]);
				if (from != null)
					from.links.remove(tokens[4]);
				
				server serv = server.findServerAbsolute(tokens[4]);
				if (serv == null)
					serv = new server(tokens[4]);
				serv.split(tokens[7]);
				
				for (event e : event.getEvents())
					e.onServerSplit(serv, from);
			}
		}
	}
}


class messagePing extends message
{
	public messagePing()
	{
		super("PING");
	}

	@Override
	public void run(String source, String[] message)
	{
		moo.sock.write("PONG :" + message[0]);
	}
}

class messagePrivmsg extends message
{
	public messagePrivmsg()
	{
		super("PRIVMSG");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2)
			return;

		if (message[1].equals("\1VERSION\1"))
			moo.notice(source, "\1VERSION " + moo.conf.getVersion() + "\1");
		else if (message[1].equals("\1TIME\1"))
			moo.notice(source, "\1TIME " + (new Date().toString()) + "\1");
		else if (message[1].startsWith("\1ACTION pets " + moo.conf.getNick()))
			moo.privmsg(message[0], "\1ACTION moos\1");
		else if (message[1].startsWith("\1ACTION milks " + moo.conf.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			moo.privmsg(message[0], "\1ACTION kicks " + nick + " in the face\1");
		}
		else if (message[1].startsWith("\1ACTION brands " + moo.conf.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			boolean kill = new Random().nextInt(100) == 0;
			
			if (kill == false)
				moo.privmsg(message[0], "\1ACTION headbutts " + nick + " and proceeds to stop on their lifeless body");
			else
			{
				moo.privmsg(message[0], "HOW DARE YOU ATTEMT TO BRAND MOO");
			}
		}
	}
}

public class messages
{
	public static final void initMessages()
	{
		new message001();
		new message015();
		new message213();
		new message227();
		new message243();
		new message364();
		new message365();
		new message474();
		new messageInvite();
		new messageNotice();
		new messagePing();
		new messagePrivmsg();
	}
}
