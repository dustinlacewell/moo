package net.rizon.moo;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

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
		
		//if (ssl)
		{
//			SSLEngine engine = SSL.ctx.createSSLEngine();
//			engine.setUseClientMode(false);
//			engine.setWantClientAuth(true); // Allows clients to send us a cert

//			pipeline.addLast("ssl", new SslHandler(engine));
		}

		pipeline.addLast("framer", new DelimiterBasedFrameDecoder(MAXBUF, Delimiters.lineDelimiter()));

		pipeline.addLast("decoder", new StringDecoder());
		pipeline.addLast("decoder", new IRCDecoder());
		
		pipeline.addLast("encoder", new StringEncoder());
		pipeline.addLast("encoder", new IRCEncoder());
		
		//p.addLast(new LoggingHandler(LogLevel.INFO));

		pipeline.addLast("handler", new ClientHandler(moo));
	}
}
