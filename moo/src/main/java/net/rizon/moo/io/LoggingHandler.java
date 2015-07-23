package net.rizon.moo.io;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import java.util.logging.Level;
import net.rizon.moo.Logger;

public class LoggingHandler extends ChannelHandlerAdapter
{
	private static final Logger log = Logger.getLogger(LoggingHandler.class.getName());
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		log.log(Level.FINE, "<- " + msg);
		ctx.fireChannelRead(msg);
	}
	
	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
	{
		log.log(Level.FINE, "-> " + msg);
		ctx.write(msg, promise);
	}
}
