package net.rizon.moo.random;

class nurPattern extends floodList
{
	private static nurPattern self = new nurPattern();
	
	@Override
	public String toString()
	{
		return "NUH";
	}
	
	protected static floodList matches(nickData nd)
	{
		if (self.isClosed)
		{
			/* My list has been closed (and thus detached from everything), so start a new list. */
			self = new nurPattern();
		}
		
		if (nd.realname_str.startsWith(nd.nick_str) && nd.nick_str.startsWith(nd.user_str))
			return self;
		else
			return null;
	}
}