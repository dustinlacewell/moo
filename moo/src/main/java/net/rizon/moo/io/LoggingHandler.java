package net.rizon.moo.io;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;

public class LoggingHandler extends ChannelHandlerAdapter
{
	@Inject
	private static Logger logger;
	
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
