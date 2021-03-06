package net.rizon.moo.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

public class LineBasedFrameEncoder extends MessageToMessageEncoder<String>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, String message, List<Object> out) throws Exception
	{
		out.add(message + "\r\n");
	}
}
