package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import net.rizon.moo.io.IRCDecoder;
import net.rizon.moo.io.IRCEncoder;
import net.rizon.moo.io.LineBasedFrameEncoder;
import net.rizon.moo.io.LoggingHandler;

class SCheckInitializer extends ChannelInitializer<SocketChannel>
{
	private static final int MAXBUF = 512;
	
	private SCheck scheck;
	
	public SCheckInitializer(SCheck scheck)
	{
		this.scheck = scheck;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception
	{
		ChannelPipeline pipeline = ch.pipeline();
		
		if (scheck.isSsl())
		{
			SslContext sslCtx = SslContext.newClientContext(null, null, InsecureTrustManagerFactory.INSTANCE, null, null, null, null, null, IdentityCipherSuiteFilter.INSTANCE, null, 16, 10);

			pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()));
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

		pipeline.addLast("clientHandler", new SCheckClientHandler(scheck));
	}
}
