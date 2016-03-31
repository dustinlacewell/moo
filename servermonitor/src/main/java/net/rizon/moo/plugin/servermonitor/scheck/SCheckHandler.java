package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;

class SCheckHandler extends SimpleChannelInboundHandler
{
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
	{
		if (evt instanceof IdleStateEvent)
		{
			ctx.close();
		}

		ctx.fireUserEventTriggered(evt);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object i) throws Exception
	{
		ctx.fireChannelRead(i);
	}
}
