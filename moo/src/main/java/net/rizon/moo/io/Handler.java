package net.rizon.moo.io;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import java.util.logging.Level;
import net.rizon.moo.Moo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Handler extends ChannelHandlerAdapter
{
	private static final Logger logger = LoggerFactory.getLogger(Handler.class);
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
					Moo.write("PING", "moo");
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
