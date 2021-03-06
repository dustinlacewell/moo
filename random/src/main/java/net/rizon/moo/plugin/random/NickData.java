package net.rizon.moo.plugin.random;

import java.util.Iterator;
import java.util.LinkedList;

public class NickData
{
	private random random;
	public String nick_str, user_str, realname_str, ip;
	public long time;
	private LinkedList<FloodList> lists = new LinkedList<FloodList>();
	public boolean dead = false, akilled = false;
	protected int hits;

	public NickData(random random, String nick, String user, String real, String ip)
	{
		this.random = random;
		this.nick_str = nick;
		this.user_str = user;
		this.realname_str = real;
		this.ip = ip;
		this.time = System.currentTimeMillis() / 1000L;

		this.lists.add(Pattern.getOrCreatePattern(random, Field.FIELD_NICK, nick));
		this.lists.add(Pattern.getOrCreatePattern(random, Field.FIELD_IDENT, user));
		this.lists.add(Pattern.getOrCreatePattern(random, Field.FIELD_GECOS, real));

		FloodList p = NURPattern.matches(random, this);
		if (p != null)
			this.lists.add(p);

		p = Previous.matches(random, this);
		if (p != null)
			this.lists.add(p);
	}

	@Override
	public String toString()
	{
		if (this.getActiveListCount() == 0)
			return this.nick_str + " " + this.user_str + "@" + this.ip + " {" + this.realname_str + "}";
		else
			return this.nick_str + " " + this.user_str + "@" + this.ip + " {" + this.realname_str + "} (" + this.getActiveListCount() + ")";
	}

	public boolean isEqual(NickData nd)
	{
		return this.nick_str.equals(nd.nick_str) && this.user_str.equals(nd.user_str) && this.realname_str.equals(nd.realname_str) && this.ip.equals(nd.ip);
	}

	public void addToLists()
	{
		for (int i = 0; i < this.lists.size(); ++i)
			this.lists.get(i).addMatch(this);
	}

	public void delFromLists()
	{
		for (int i = 0; i < this.lists.size(); ++i)
			this.lists.get(i).delMatch(this);
		dead = true;
	}

	public void addList(FloodList l)
	{
		if (dead)
			return;
		this.lists.addLast(l);
		l.addMatch(this);
	}

	public int getActiveListCount()
	{
		int activeLists = 0;
		for (Iterator<FloodList> it = this.lists.iterator(); it.hasNext();)
			if (it.next().isList)
				++activeLists;
		return activeLists;
	}
}
