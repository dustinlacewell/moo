package net.rizon.moo;

public class CommandSource
{
	private User user;
	private Nameable dest;

	public CommandSource(User user, Nameable dest)
	{
		this.user = user;
		this.dest = dest;
	}

	public User getUser()
	{
		return user;
	}

	public String getTargetName()
	{
		return dest.getName();
	}

	public void reply(String message)
	{
		Moo.reply(user.getNick(), dest.getName(), message);
	}

	public void notice(String message)
	{
		Moo.notice(dest.getName(), message);
	}
}
