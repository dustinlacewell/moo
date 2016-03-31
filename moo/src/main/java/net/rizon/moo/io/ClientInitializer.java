package net.rizon.moo.io;

import com.google.inject.Inject;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LineBasedFrameDecoder;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.IdleStateHandler;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import net.rizon.moo.conf.Config;

public class ClientInitializer extends ChannelInitializer<SocketChannel>
{
	private static final int MAXBUF = 512;
	
	@Inject
	private Handler handler;
	
	@Inject
	private InboundLoggingHandler inboundLoggingHandler;

	@Inject
	private OutboundLoggingHandler outboundLoggingHandler;
	
	@Inject
	private ClientHandler clientHandler;

	@Inject
	private Config conf;
	
	@Override
	protected void initChannel(SocketChannel ch) throws Exception
	{
		ChannelPipeline pipeline = ch.pipeline();
		
		if (conf.general.ssl)
		{
			SslContext sslCtx;
			
			if (conf.general.cert != null && conf.general.key != null)
			{
				CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
				
				byte[] certBytes = Files.readAllBytes(new File(conf.general.cert).toPath());
				byte[] keyBytes = Files.readAllBytes(new File(conf.general.key).toPath());
				
				X509Certificate cert = (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(certBytes));
				PrivateKey key = (PrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(keyBytes));
				
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(null);
				ks.setCertificateEntry("moo", cert);
				ks.setKeyEntry("moo", key, "".toCharArray(), new Certificate[] { cert });
				
				KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
				kmf.init(ks, "".toCharArray());
				
				SSLContext clientContext = SSLContext.getInstance("TLS");
				clientContext.init(kmf.getKeyManagers(), InsecureTrustManagerFactory.INSTANCE.getTrustManagers(), null);
				
				SSLEngine engine = clientContext.createSSLEngine();
				engine.setUseClientMode(true);
				
				pipeline.addLast("ssl", new SslHandler(engine));
			}
			else
			{
				sslCtx = SslContext.newClientContext(null, null, InsecureTrustManagerFactory.INSTANCE, null, IdentityCipherSuiteFilter.INSTANCE, null, 16, 10);
				pipeline.addLast("ssl", sslCtx.newHandler(ch.alloc()));
			}
		}

		pipeline.addLast("frameDecoder", new LineBasedFrameDecoder(MAXBUF));
		pipeline.addLast("stringDecoder", new StringDecoder());
		pipeline.addLast("ircDecoder", new IRCDecoder());
		
		pipeline.addLast("stringEncoder", new StringEncoder());
		pipeline.addLast("frameEncoder", new LineBasedFrameEncoder());
		pipeline.addLast("ircEncoder", new IRCEncoder());
		
		pipeline.addLast("idleStateHandler", new IdleStateHandler(60, 0, 0));
		pipeline.addLast("handler", handler);
		
		pipeline.addLast(inboundLoggingHandler);
		pipeline.addLast(outboundLoggingHandler);

		pipeline.addLast("clientHandler", clientHandler);
	}
}
