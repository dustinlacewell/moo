package net.rizon.moo.io;

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.netty.channel.Channel;
import net.rizon.moo.Moo;

public class ChannelProvider implements Provider<Channel>
{
	@Override
	public Channel get()
	{
		return Moo.channel; // XXX
	}

}
