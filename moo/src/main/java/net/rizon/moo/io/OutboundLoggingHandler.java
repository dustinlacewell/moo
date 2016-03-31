package net.rizon.moo.io;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import org.slf4j.Logger;

public class OutboundLoggingHandler extends ChannelOutboundHandlerAdapter
{
	@Inject
	private static Logger logger;

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception
	{
		logger.debug("-> {}", msg);

		ctx.write(msg, promise);
	}
}
