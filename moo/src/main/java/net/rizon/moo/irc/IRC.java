package net.rizon.moo.irc;

import java.util.HashMap;
import java.util.Map;

public class IRC
{
	private final Map<String, Channel> channels = new HashMap<>();
	private final Map<String, User> users = new HashMap<>();
	
	private String ircToLower(String in)
	{
		return in.toLowerCase().replace('[', '{').replace('\\', '|').replace(']', '}').replace('^', '~');
	}
	
	public void insertChannel(Channel c)
	{
		channels.put(ircToLower(c.getName()), c);
	}
	
	public Channel findChannel(String name)
	{
		return channels.get(ircToLower(name));
	}
	
	public void removeChannel(Channel channel)
	{
		channels.remove(ircToLower(channel.getName()));
	}
	
	public void insertUser(User u)
	{
		users.put(ircToLower(u.getNick()), u);
	}
	
	public User findUser(String name)
	{
		return users.get(ircToLower(name));
	}
	
	public void removeUser(User user)
	{
		users.remove(ircToLower(user.getNick()));
	}
}
