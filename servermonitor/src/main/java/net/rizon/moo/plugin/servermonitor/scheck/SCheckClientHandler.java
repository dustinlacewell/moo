package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.rizon.moo.io.IRCMessage;

class SCheckClientHandler extends SimpleChannelInboundHandler<IRCMessage>
{
	private SCheck scheck;
	
	public SCheckClientHandler(SCheck scheck)
	{
		this.scheck = scheck;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		scheck.handshake();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IRCMessage message) throws Exception
	{
		scheck.process(message);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
	{
		scheck.exceptionCaught(cause);
	}
}
