package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.rizon.moo.IRCMessage;
import net.rizon.moo.Message;

class SCheckClientHandler extends SimpleChannelInboundHandler<IRCMessage>
{
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		//moo.handshake();
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, IRCMessage message) throws Exception
	{
	}
}
