package net.rizon.moo.io;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ClientHandler extends SimpleChannelInboundHandler<IRCMessage>
{
	private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);

	private Moo moo;
	
	public ClientHandler(Moo moo)
	{
		this.moo = moo;
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx)
	{
		moo.handshake();
	}

	@Override
	protected void messageReceived(ChannelHandlerContext ctx, IRCMessage message) throws Exception
	{
		Message.runMessage(message);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
	{
		logger.error("exception caught in handler for context " + ctx, cause);
	}
}
