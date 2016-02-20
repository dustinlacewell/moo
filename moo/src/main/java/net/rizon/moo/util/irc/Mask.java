package net.rizon.moo.util.irc;

public class Mask
{
	private String nick, username, host;

	public Mask(String nick, String username, String host)
	{
		this.nick = nick;
		this.username = username;
		this.host = host;
	}

	public String getNick()
	{
		return nick;
	}

	public String getUsername()
	{
		return username;
	}

	public String getHost()
	{
		return host;
	}
}
