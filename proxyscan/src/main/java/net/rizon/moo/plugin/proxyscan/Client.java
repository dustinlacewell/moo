package net.rizon.moo.plugin.proxyscan;

import java.util.Objects;

public class Client
{
	private final String nick;
	private final String ident;
	private final String ip;
	private final String gecos;

	public Client(String nick, String ident, String host, String gecos)
	{
		this.nick = nick;
		this.ident = ident;
		this.ip = host;
		this.gecos = gecos;
	}

	@Override
	public String toString()
	{
		return "Client{" + "nick=" + nick + ", ident=" + ident + ", ip=" + ip + ", gecos=" + gecos + '}';
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

	public String getGecos()
	{
		return gecos;
	}
}
