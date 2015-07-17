package net.rizon.moo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;

class IRCDecoder extends MessageToMessageDecoder<String>
{
	@Override
	protected void decode(ChannelHandlerContext ctx, String message, List<Object> out) throws Exception
	{
		String[] tokens = message.split(" ");
		if (tokens.length < 2)
			return;

		String source = null;
		int begin = 0;
		if (tokens[begin].startsWith(":"))
			source = tokens[begin++].substring(1);

		String message_name = tokens[begin++];

		int end = begin;
		for (; end < tokens.length; ++end)
			if (tokens[end].startsWith(":"))
				break;
		if (end == tokens.length)
			--end;

		String[] buffer = new String[end - begin + 1];
		int buffer_count = 0;

		for (int i = begin; i < end; ++i)
			buffer[buffer_count++] = tokens[i];

		if (buffer.length > 0)
			buffer[buffer_count] = tokens[end].startsWith(":") ? tokens[end].substring(1) : tokens[end];
		for (int i = end + 1; i < tokens.length; ++i)
			buffer[buffer_count] += " " + tokens[i];
		
		IRCMessage ircMessage = new IRCMessage(source, message_name, buffer);
		out.add(ircMessage);
	}
}
