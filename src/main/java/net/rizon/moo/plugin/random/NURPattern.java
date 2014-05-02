package net.rizon.moo.plugin.random;

class NURPattern extends FloodList
{
	private static NURPattern self;
	
	@Override
	public String toString()
	{
		return "NUR";
	}
	
	protected static FloodList matches(NickData nd)
	{	
		if (nd.realname_str.startsWith(nd.nick_str) && nd.nick_str.startsWith(nd.user_str))
		{
			if (self == null || self.isClosed)
			{
				/* My list has been closed (and thus detached from everything), so start a new list. */
				self = new NURPattern();
				self.open();
			}
			
			return self;
		}
		else
			return null;
	}
}