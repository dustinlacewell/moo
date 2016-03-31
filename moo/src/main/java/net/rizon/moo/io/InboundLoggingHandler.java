package net.rizon.moo.io;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;

public class InboundLoggingHandler extends SimpleChannelInboundHandler
{
	@Inject
	private static Logger logger;
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		logger.debug("<- {}", msg);
		
		ctx.fireChannelRead(msg);
	}
}
