package net.rizon.moo.io;

import com.google.inject.Inject;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.rizon.moo.MessageManager;
import net.rizon.moo.irc.Protocol;
import org.slf4j.Logger;

@Sharable
class ClientHandler extends SimpleChannelInboundHandler<IRCMessage>
{
	@Inject
	private static Logger logger;
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private MessageManager messageManager;
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		protocol.handshake();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, IRCMessage message) throws Exception
	{
		messageManager.run(message);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		logger.error("exception caught in handler for context " + ctx, cause);
	}
}
