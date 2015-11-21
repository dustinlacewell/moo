package net.rizon.moo.events;

import net.rizon.moo.Event;

public class EventClientConnect extends Event
{
	private String nick, ident, ip, realname;

	public EventClientConnect(String nick, String ident, String ip, String realname)
	{
		this.nick = nick;
		this.ident = ident;
		this.ip = ip;
		this.realname = realname;
	}

	public String getNick()
	{
		return nick;
	}

	public String getIdent()
	{
		return ident;
	}

	public String getIp()
	{
		return ip;
	}

	public String getRealname()
	{
		return realname;
	}
}
