package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;

class SCheckHandler extends ChannelHandlerAdapter
{
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
	{
		if (evt instanceof IdleStateEvent)
		{
			ctx.close();
		}
	}
}
