package net.rizon.moo.random;

class nurPattern extends floodList
{
	private static nurPattern self;
	
	@Override
	public String toString()
	{
		return "NUR";
	}
	
	protected static floodList matches(nickData nd)
	{	
		if (nd.realname_str.startsWith(nd.nick_str) && nd.nick_str.startsWith(nd.user_str))
		{
			if (self.isClosed)
			{
				/* My list has been closed (and thus detached from everything), so start a new list. */
				self = new nurPattern();
				self.open();
			}
			
			return self;
		}
		else
			return null;
	}
}