package net.rizon.moo.irc;

import java.util.ArrayList;

public class UserManager extends EntityManager<User>
{
	public void renameUser(User u, String newnick)
	{
		this.remove(u);
		u.setNick(newnick);
		this.add(u);
	}

	public void quit(User u)
	{
		ArrayList<Membership> mems = new ArrayList<Membership>(u.getChannels());
		for (Membership m : mems)
		{
			Channel c = m.getChannel();

			u.removeChannel(m);
			c.removeUser(m);
		}

		this.remove(u);
	}

	public User findOrCreateUser(String mask)
	{
		String nick = mask;

		if (mask.contains("!"))
			nick = mask.split("!")[0];

		User u = this.find(nick);
		if (u == null)
		{
			u = new User(nick);
			this.add(u);
		}

		return u;
	}

	@Override
	public User find(String nick)
	{
		User u;

		if ((u = super.find(nick)) != null)
			return u;

		return super.find(nick.split("!")[0]);
	}
}
