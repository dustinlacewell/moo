package net.rizon.moo;

import java.util.Collection;
import java.util.HashMap;

/**
 * Tracks IRC channels and their users.
 */
public class Channel implements Nameable
{
	private final String name;
	private final HashMap<User, Membership> users = new HashMap<User, Membership>();

	public Channel(final String name)
	{
		this.name = name;
	}

	@Override
	public String getName()
	{
		return this.name;
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
