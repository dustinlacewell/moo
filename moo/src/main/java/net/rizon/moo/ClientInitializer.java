package net.rizon.moo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;

class ClientInitializer extends ChannelInitializer<SocketChannel>
{
	private static final int MAXBUF = 512;
	
	private Moo moo;
	
	public ClientInitializer(Moo moo)
	{
		this.moo = moo;
	}
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception
	{
		ChannelPipeline pipeline = ch.pipeline();
		
		if (Moo.conf.general.ssl)
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
		
		pipeline.addLast("idleStateHandler", new IdleStateHandler(120, 60, 0));
		pipeline.addLast("handler", new Handler(moo));
		
		//pipeline.addLast(new LoggingHandler());

		pipeline.addLast("clientHandler", new ClientHandler(moo));
	}
}
