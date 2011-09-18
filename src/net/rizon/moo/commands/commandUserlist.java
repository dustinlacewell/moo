package net.rizon.moo.commands;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.server;

class userdata
{
	public String nick, ident, host, ip;
	
	public void insert()
	{
		data.put(this.nick, this);
	}
	
	public final boolean matches(final String mask)
	{
		if (moo.match(this.nick, mask))
			return true;
		
		String maskbuf = this.nick + "!" + this.ident + "@" + this.host;
		if (moo.match(maskbuf, mask))
			return true;
		
		maskbuf = this.nick + "!" + this.ident + "@" + this.ip;
		if (moo.match(maskbuf, mask))
			return true;
		
		return false;
	}
	
	private static HashMap<String, userdata> data = new HashMap<String, userdata>();
	
	public static final Set<String> getUsers()
	{
		return data.keySet();
	}
	
	public static userdata getUser(final String user)
	{
		return data.get(user);
	}
	
	public static void clear()
	{
		data.clear();
	}
}

class message205 extends message
{
	public message205()
	{
		super("205");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 5)
			return;
		
		int op = message[3].indexOf('['), at = message[3].indexOf('@');

		userdata ud = new userdata();
		ud.nick = message[3].substring(0, op);
		ud.ident = message[3].substring(op + 1, at);
		ud.host = message[3].substring(at + 1, message[3].length() - 1);
		ud.ip = message[4].substring(1, message[4].length() - 1);
		ud.insert();
	}
}

class message262 extends message
{
	public message262()
	{
		super("262");
	}
	
	public static HashSet<String> waiting_on = new HashSet<String>();
	public static String target_source = null;
	public static String target_target = null;
	public static String match = null;

	@Override
	public void run(String source, String[] message)
	{
		waiting_on.remove(source);
		
		if (waiting_on.isEmpty())
		{
			int count = 0;
			for (Iterator<String> it = userdata.getUsers().iterator(); it.hasNext();)
			{
				userdata ud = userdata.getUser(it.next());
				
				if (ud.matches(match))
				{
					++count;
					moo.sock.reply(target_source, target_target, "Match: " + ud.nick + "!" + ud.ident + "@" + ud.host + "(" + ud.ip + ")");
				}
			}
			
			moo.sock.reply(target_source, target_target, count + " matches");
			
			userdata.clear();
			waiting_on.clear();
			target_source = target_target = match = null;
		}
	}
}

public class commandUserlist extends command
{
	@SuppressWarnings("unused")
	private message205 msg_205 = new message205();
	@SuppressWarnings("unused")
	private message262 msg_262 = new message262();

	public commandUserlist()
	{
		super("!USERLIST", "Lookup users");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
			return;
		
		userdata.clear();
		
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();
			if (s.getSID().endsWith("C") || s.getSID().endsWith("H") || s.getSID().endsWith("Z"))
			{
				moo.sock.write("TRACE " + s.getName());
				message262.waiting_on.add(s.getName());
			}
		}
		
		message262.target_source = source;
		message262.target_target = target;
		message262.match = params[1];
	}
}
