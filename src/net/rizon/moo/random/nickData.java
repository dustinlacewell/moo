package net.rizon.moo.random;

public class nickData
{
	private pattern nick_p, user_p, realname_p;
	public String nick_str, user_str, realname_str, ip;
	
	public nickData(final String nick, final String user, final String real, final String ip)
	{
		this.nick_str = nick;
		this.user_str = user;
		this.realname_str = real;
		this.ip = ip;
		
		this.nick_p = pattern.getOrCreatePattern(nick);
		this.user_p = pattern.getOrCreatePattern(user);
		this.realname_p = pattern.getOrCreatePattern(real);
	}
	
	public void inc()
	{
		this.nick_p.add(this, this.nick_str);
		this.user_p.add(this, this.user_str);
		this.realname_p.add(this, this.realname_str);
	}
	
	public void dec()
	{
		this.nick_p.del(this);
		this.user_p.del(this);
		this.realname_p.del(this);
	}
}
