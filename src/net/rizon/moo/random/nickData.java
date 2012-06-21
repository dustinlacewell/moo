package net.rizon.moo.random;

public class nickData
{
	private pattern nick_p, user_p, realname_p;
	public String nick_str, user_str, realname_str, ip;
	public long time;
	
	public nickData(final String nick, final String user, final String real, final String ip)
	{
		this.nick_str = nick;
		this.user_str = user;
		this.realname_str = real;
		this.ip = ip;
		this.time = System.currentTimeMillis() / 1000L;
		
		this.nick_p = pattern.getOrCreatePattern(field.FIELD_NICK, nick);
		this.user_p = pattern.getOrCreatePattern(field.FIELD_IDENT, user);
		this.realname_p = pattern.getOrCreatePattern(field.FIELD_GECOS, real);
	}
	
	@Override
	public String toString()
	{
		return this.nick_str + "!" + this.user_str + "@" + this.ip + "{" + this.realname_str + "}";
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
	
	public int getScore()
	{
		int s = 0;
		if (frequency.isRandom(this.nick_str))
			s += 2;
		if (frequency.isRandom(this.user_str))
			s += 2;
		if (frequency.isRandom(this.realname_str))
			s += 2;
		if (frequency.containsBigrams(this.nick_str))
			--s;
		if (frequency.containsBigrams(this.user_str))
			--s;
		if (frequency.containsBigrams(this.realname_str))
			--s;
		return s;
	}
}
