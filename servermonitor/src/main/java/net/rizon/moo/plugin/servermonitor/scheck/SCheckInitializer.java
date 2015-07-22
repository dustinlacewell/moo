package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import net.rizon.moo.IRCDecoder;
import net.rizon.moo.IRCEncoder;

class SCheckInitializer extends ChannelInitializer<SocketChannel>
{
	private static final int MAXBUF = 512;
	
	private boolean ssl;
	
	public SCheckInitializer(boolean ssl)
	{
		this.ssl = ssl;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception
	{
		ChannelPipeline pipeline = ch.pipeline();
		
		if (ssl)
		{
			SslContext sslCtx = SslContext.newClientContext(InsecureTrustManagerFactory.INSTANCE);

			pipeline.addLast(sslCtx.newHandler(ch.alloc()));
		}

		pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(MAXBUF));
		pipeline.addLast("stringDecoder", new StringDecoder());
		pipeline.addLast("ircDecoder", new IRCDecoder());
		
		pipeline.addLast("stringEncoder", new StringEncoder());
		pipeline.addLast("frameEncoder", new LineBasedFrameEncoder());
		pipeline.addLast("ircEncoder", new IRCEncoder());
		
		pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 60, 0));
		pipeline.addLast("handler", new SCheckHandler());
		
		pipeline.addLast(new LoggingHandler());

		pipeline.addLast("clientHandler", new SCheckClientHandler());
	}
}
