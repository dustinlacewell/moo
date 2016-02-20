package net.rizon.moo.io;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.rizon.moo.irc.Protocol;
import org.slf4j.Logger;

public class Handler extends ChannelHandlerAdapter
{
	@Inject
	private static Logger logger;
	
	@Inject
	private Protocol protocol;
	
	private boolean idle;
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
	{
		idle = false;
		super.channelRead(ctx, msg);
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
	{
		if (evt instanceof IdleStateEvent)
		{
			IdleStateEvent e = (IdleStateEvent) evt;

			if (e.state() == IdleState.READER_IDLE)
			{
				if (!idle)
				{
					protocol.write("PING", "moo");
					idle = true;
				}
				else
				{
					logger.warn("No read from uplink in 120 seconds, closing connection");

					ctx.close();
				}
			}
		}
	}
}
