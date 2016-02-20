package net.rizon.moo.irc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Tracks IRC channels and their users.
 */
public class Channel
{
	private final String name;
	private final Map<User, Membership> users = new HashMap<>();

	public Channel(final String name)
	{
		this.name = name;
	}
	
	public String getName()
	{
		return name;
	}
	
	public void addUser(Membership mem)
	{
		assert mem.getChannel() == this;
		this.users.put(mem.getUser(), mem);
	}

	public void removeUser(Membership mem)
	{
		assert mem.getChannel() == this;
		this.users.remove(mem.getUser());
	}

	public Collection<Membership> getUsers()
	{
		return users.values();
	}

	public Membership findUser(User user)
	{
		return users.get(user);
	}
}
