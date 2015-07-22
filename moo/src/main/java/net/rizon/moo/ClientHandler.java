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
	public void channelActive(ChannelHandlerContext ctx)
	{
		moo.handshake();
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, IRCMessage message) throws Exception
	{
		Message.runMessage(message);
	}
}
