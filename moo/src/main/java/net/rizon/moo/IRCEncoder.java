package net.rizon.moo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import java.util.List;

class IRCEncoder extends MessageToMessageEncoder<IRCMessage>
{
	@Override
	protected void encode(ChannelHandlerContext ctx, IRCMessage msg, List<Object> out) throws Exception
	{
		StringBuilder builder = new StringBuilder();
		
		if (msg.getSource() != null)
			builder.append(':').append(msg.getSource()).append(' ');
		
		builder.append(msg.getCommand());
		
		String[] params = msg.getParams();
		for (int i = 0; i < params.length; ++i)
		{
			builder.append(' ');
			if (i + 1 == params.length)
				builder.append(':');
			builder.append(params[i]);
		}
		
		out.add(builder.toString());
	}
	
}
