package net.rizon.moo.plugin.random;

class NURPattern extends FloodList
{
	private static NURPattern self;

	public NURPattern(random random)
	{
		super(random);
	}

	@Override
	public String toString()
	{
		return "NUR";
	}

	protected static FloodList matches(random random, NickData nd)
	{
		if (nd.realname_str.startsWith(nd.nick_str) && nd.nick_str.startsWith(nd.user_str))
		{
			if (self == null || self.isClosed)
			{
				/* My list has been closed (and thus detached from everything), so start a new list. */
				self = new NURPattern(random);
				self.open();
			}

			return self;
		}
		else
			return null;
	}
}