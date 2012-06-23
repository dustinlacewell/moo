package net.rizon.moo.random;

public class globalFloodList extends floodList
{
	public globalFloodList()
	{
		this.isList = true;
	}
	
	@Override
	public String toString()
	{
		return "Global flood";
	}

	@Override
	public void onClose()
	{
		random.globalFlood = null;
	}
}
