package net.rizon.moo;

public class ChannelManager extends EntityManager<Channel>
{
	public Channel findOrCreateChannel(String name)
	{
		Channel c = this.entities.get(name.toLowerCase());

		if (c == null)
		{
			c = new Channel(name);
			this.add(c);
		}

		return c;
	}
}
