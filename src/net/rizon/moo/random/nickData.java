package net.rizon.moo.random;

import java.util.Iterator;
import java.util.LinkedList;

public class nickData
{
	public String nick_str, user_str, realname_str, ip;
	public long time;
	private LinkedList<floodList> lists = new LinkedList<floodList>();
	public boolean akilled = false;
	
	public nickData(final String nick, final String user, final String real, final String ip)
	{
		this.nick_str = nick;
		this.user_str = user;
		this.realname_str = real;
		this.ip = ip;
		this.time = System.currentTimeMillis() / 1000L;
		
		this.lists.add(pattern.getOrCreatePattern(field.FIELD_NICK, nick));
		this.lists.add(pattern.getOrCreatePattern(field.FIELD_IDENT, user));
		this.lists.add(pattern.getOrCreatePattern(field.FIELD_GECOS, real));
		
		floodList p = nurPattern.matches(this);
		if (p != null)
			this.lists.add(p);

		p = previous.matches(this);
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
	
	public void addToLists()
	{
		for (int i = 0; i < this.lists.size(); ++i)
			this.lists.get(i).addMatch(this);
	}
	
	public void delFromLists()
	{
		for (int i = 0; i < this.lists.size(); ++i)
			this.lists.get(i).delMatch(this);
	}
	
	public int getActiveListCount()
	{
		int activeLists = 0;
		for (Iterator<floodList> it = this.lists.iterator(); it.hasNext();)
			if (it.next().isList)
				++activeLists;
		return activeLists;
	}
}
