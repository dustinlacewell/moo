package net.rizon.moo.io;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import net.rizon.moo.Moo;

public class Handler extends ChannelHandlerAdapter
{
	private final Moo moo;
	
	public Handler(Moo moo)
	{
		this.moo = moo;
	}
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
	{
		if (evt instanceof IdleStateEvent)
		{
			IdleStateEvent e = (IdleStateEvent) evt;

			if (e.state() == IdleState.READER_IDLE)
			{
				ctx.close();
			}
			else if (e.state() == IdleState.WRITER_IDLE)
			{
				moo.write("PING", "moo");
			}
		}
	}
}
