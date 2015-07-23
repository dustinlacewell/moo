package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import java.util.logging.Level;
import javax.net.ssl.SSLPeerUnverifiedException;
import net.rizon.moo.Logger;

import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.io.IRCMessage;

public class SCheck
{
	private static final Logger log = Logger.getLogger(SCheck.class.getName());
	
	private Server server;
	private int port;
	private boolean ssl;
	private String[] targets;
	private String users;
	private String servers;
	private boolean quiet;
	private boolean use_v6;
	private final String prefix;
	
	private Channel channel;

	private static final Random rand = new Random();
	private static String getRandom()
	{
		String buf = "";
		for (int i = 0; i < 5; ++i)
		{
			char c;
			do
			{
				int j = rand.nextInt(57);
				j += 65;
				c = (char) j;
			}
			while (Character.isLetter(c) == false);
			buf += c;
		}

		return buf;
	}
	
	public void write(String command, Object... args)
	{
		if (channel == null)
			return;
		
		String[] params = new String[args.length];
		int i = 0;
		for (Object o : args)
			params[i++] = o.toString();
		
		IRCMessage message = new IRCMessage(null, command, params);
		channel.writeAndFlush(message);
	}

	private void reply(final String msg)
	{
		for (String s : this.targets)
			Moo.reply(null, s, msg);
	}

	public SCheck(Server serv, String[] targets, boolean ssl, int port, boolean quiet, boolean use_v6)
	{
		this.server = serv;
		this.targets = targets;
		this.ssl = ssl;
		this.port = port;
		this.quiet = quiet;
		this.use_v6 = use_v6;
		this.prefix = use_v6 ? "[SCHECK6] " : "[SCHECK] ";
	}
	
	public void handshake()
	{
		write("USER", Moo.conf.general.ident, ".", ".", Moo.conf.general.realname);
		write("NICK", Moo.conf.general.nick + "-" + getRandom());
	}
	
	public void process(IRCMessage message)
	{
		String command = message.getCommand();
		String[] messageParams = message.getParams();
		
		if (command.equals("PING") && messageParams.length > 0)
		{
			write("PONG", messageParams[0]);
		}
		else if (command.equals("251"))
		{
			String[] params = message.getParams()[1].split(" ");
			this.servers = params[8];
		}
		else if (command.equals("266"))
		{
			try
			{
				// Checks if the 266 message is returned by P3.
				this.users = Integer.toString(Integer.parseInt(message.getParams()[1]));
			}
			catch (NumberFormatException e)
			{
				String[] params = message.getParams()[1].split(" ");
				// Probably P4.
				this.users = params[3];
			}
			write("STATS", "u");
		}
		else if (command.equals("242"))
		{
			String[] params = message.getParams()[1].split(" ");
			String servername = message.getSource();

			if (this.ssl == true)
			{
				SslHandler handler = (SslHandler) channel.pipeline().get("ssl");
				Certificate[] certs;
				
				try
				{
					certs = handler.engine().getSession().getPeerCertificates();
				}
				catch (SSLPeerUnverifiedException ex)
				{
					return;
				}
				
				Certificate cert = certs[0];

				if (cert instanceof X509Certificate)
				{
					X509Certificate x509 = (X509Certificate) cert;

					if (this.server.cert == null)
						this.server.cert = x509;
					else if (!this.server.cert.equals(x509))
					{
						final String oldDN = this.server.cert.getIssuerDN().getName(), newDN = x509.getIssuerDN().getName();
						if (!oldDN.equals(newDN))
							reply(this.prefix + "[INFO] SSL certificate for " + this.server.getName() + " has changed from \"" + oldDN + "\" to \"" + newDN + "\"");
						else
							reply(this.prefix + "[INFO] SSL certificate for " + this.server.getName() + " has changed");
						this.server.cert = x509;
					}

					try
					{
						Date now = new Date();
						x509.checkValidity(now);

						try
						{
							Calendar c = Calendar.getInstance();
							c.setTime(now);
							c.add(Calendar.DATE, 1);
							x509.checkValidity(c.getTime());
						}
						catch (CertificateExpiredException e)
						{
							reply(this.prefix + "[WARNING] SSL certificate for " + server.getName() + " expires on " + server.cert.getNotAfter() + ", which is " + Moo.difference(now, server.cert.getNotAfter()) + " from now");
						}
					}
					catch (CertificateExpiredException e)
					{
						reply(this.prefix + "[WARNING] " + servername + " has EXPIRED X509 SSL certificate!");
					}
					catch (CertificateNotYetValidException e)
					{
						reply(this.prefix + "[WARNING] " + servername + " has a NOT VALID YET X509 SSL certificate!");
					}

					final String issuerDn = x509.getIssuerDN().getName();
					if (this.quiet == false)
						reply(this.prefix + servername + " has X509 certificate " + issuerDn);
					//if (issuerDn.contains("O=Rizon IRC Network, CN=irc.rizon.net, L=Rizon, ST=Nowhere, C=RZ") == false)
					//	reply(this.prefix + "[WARNING] " + servername + " does not have a correct issuer DN");
				}
				else
						reply(this.prefix + servername + " has non X509 certificate " + cert.getPublicKey() + " - " + cert.getType());
			}
			if (this.quiet == false)
				reply(this.prefix + "[" + servername + "] Global users: " + this.users + ", Servers: " + this.servers + ", Uptime: " + params[2] + " days " + params[4]);
			
			channel.close();
		}
	}
	
	public void exceptionCaught(Throwable cause)
	{
		reply(this.prefix + "Error caught in scheck: " + cause);
		log.log(Level.WARNING, "Error caught in scheck", cause);
		channel.close();
	}
	
	private InetAddress getServerIp()
	{
		InetAddress[] address;
		try
		{
			address = InetAddress.getAllByName(server.getName());
		}
		catch (UnknownHostException ex)
		{
			return null;
		}
		for (InetAddress addr : address)
		{
			if (addr instanceof Inet6Address && use_v6)
				return addr;
			else if (addr instanceof Inet4Address && !use_v6)
				return addr;
		}
		
		return null;
	}
	
	public void start()
	{
		if (this.ssl == true || this.port != 6667)
			reply(this.prefix + "Connecting to " + this.server.getName() + ":" + (this.ssl == true ? "+" : "") + this.port);
		else
			reply(this.prefix + "Connecting to " + this.server.getName() + "...");
				
		Bootstrap client = new Bootstrap()
		    .group(Moo.moo.getGroup())
		    .channel(NioSocketChannel.class)
		    .handler(new SCheckInitializer(this))
		    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 15 * 1000);
		
		if (Moo.conf.general.host != null && !use_v6)
			try
			{
				client.bind(new InetSocketAddress(Moo.conf.general.host, 0)).sync().await();
			}
			catch (InterruptedException ex) { }
		
		
		InetAddress addr = this.getServerIp();
		if (addr == null)
		{
			reply(prefix + "Unable to connect to " + server.getName() + ", unknown host");
			return;
		}

		ChannelFuture future = client.connect(addr, port);
		this.channel = future.channel();
		
		future.addListener(new ChannelFutureListener()
		{	
			@Override
			public void operationComplete(ChannelFuture future) throws Exception
			{
				Throwable ex = future.cause();
				if (ex == null)
					return;
				
				if (ex instanceof UnknownHostException)
				{
					reply(prefix + "Unable to connect to " + server.getName() + ", unknown host");
				}
				else if (ex instanceof NoRouteToHostException)
				{
					reply(prefix + "Unable to connect to " + server.getName() + ", no route to host");
				}
				else if (ex instanceof SocketTimeoutException)
				{
					reply(prefix + "Unable to connect to " + server.getName() + ", connection timeout");
				}
				else
				{
					reply(prefix + "Unable to connect to " + server.getName() + ": " + ex);
				}
			}
		});
	}
	
	public boolean isSsl()
	{
		return ssl;
	}
}
