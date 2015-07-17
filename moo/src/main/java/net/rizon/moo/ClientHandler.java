package net.rizon.moo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

class ClientHandler extends SimpleChannelInboundHandler<IRCMessage>
{
	private Moo moo;
	
	public ClientHandler(Moo moo)
	{
		this.moo = moo;
	}

	@Override
	protected void messageReceived(ChannelHandlerContext arg0, IRCMessage arg1) throws Exception
	{
	}
}
