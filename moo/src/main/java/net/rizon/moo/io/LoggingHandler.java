package net.rizon.moo.io;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingHandler extends ChannelHandlerAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(LoggingHandler.class);
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		logger.debug("<- {}", msg);
		
		ctx.fireChannelRead(msg);
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
	{
		logger.debug("-> {}", msg);

		ctx.write(msg, promise);
	}
}
