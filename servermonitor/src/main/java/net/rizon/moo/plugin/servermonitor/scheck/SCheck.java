package net.rizon.moo.plugin.servermonitor.scheck;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslHandler;
import java.net.InetSocketAddress;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import javax.net.ssl.SSLPeerUnverifiedException;

import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.io.IRCMessage;

public class SCheck
{
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
		String[] params = message.getParams();
		
		if (command.equals("PING") && params.length > 0)
		{
			write("PONG", params[0]);
		}
		else if (command.equals("251") && params.length > 10)
		{
			this.servers = params[10];
		}
		else if (command.equals("266") && params.length > 7)
		{
			try
			{
				// Checks if the 266 message is returned by P3.
				this.users = Integer.toString(Integer.parseInt(params[2]));
			}
			catch (NumberFormatException e)
			{
				// Probably P4.
				this.users = params[5];
			}
			write("STATUS", "u");
		}
		else if (command.equals("242") && params.length > 6)
		{
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
					if (issuerDn.contains("O=Rizon IRC Network, CN=irc.rizon.net, L=Rizon, ST=Nowhere, C=RZ") == false)
						reply(this.prefix + "[WARNING] " + servername + " does not have a correct issuer DN");
				}
				else
						reply(this.prefix + servername + " has non X509 certificate " + cert.getPublicKey() + " - " + cert.getType());
			}
			if (this.quiet == false)
				reply(this.prefix + "[" + servername + "] Global users: " + this.users + ", Servers: " + this.servers + ", Uptime: " + params[4] + " days " + params[6]);
			
			channel.close();
		}
	}

	/*
	@Override
	public void run()
	{
		Socket s = null;

		try
		{
			if (this.ssl == true)
				s = Socket.createSSL();
			else
				s = Socket.create();
			if (this.quiet == false)
			{
				if (this.ssl == true || this.port != 6667)
					reply(this.prefix + "Connecting to " + this.server.getName() + ":" + (this.ssl == true ? "+" : "") + this.port);
				else
					reply(this.prefix + "Connecting to " + this.server.getName() + "...");
			}

			try
			{
				s.connect(this.server.getName(), this.port, 15000, this.use_v6);
			}
			catch (UnknownHostException ex)
			{
				reply(this.prefix + "No IPv" + (this.use_v6 ? "6" : "4") + " records found for " + this.server.getName() + ".");
				return;
			}

			s.write("USER " + Moo.conf.general.ident + " . . :" + Moo.conf.general.realname);
			s.write("NICK " + Moo.conf.general.nick + "-" + getRandom());

			for (String in; (in = s.read()) != null;)
			{
				String[] token = in.split(" ");

				if (token.length >= 2 && token[0].equals("PING"))
				{
					s.write("PONG " + token[1]);
				}
				else if (token.length > 11 && token[1].equals("251"))
				{
					this.servers = token[11];
				}
				else if (token.length > 8 && token[1].equals("266"))
				{
					try
					{
						// Checks if the 266 message is returned by P3.
						this.users = Integer.toString(Integer.parseInt(token[3]));
					}
					catch (NumberFormatException e)
					{
						// Probably P4.
						this.users = token[6];
					}
					s.write("STATS u");
				}
				else if (token.length > 7 && token[1].equals("242"))
				{
					final String servername = token[0].substring(1);

					if (this.ssl == true)
					{
						Certificate[] certs = s.getSSLSocket().getSession().getPeerCertificates();
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
							if (issuerDn.contains("O=Rizon IRC Network, CN=irc.rizon.net, L=Rizon, ST=Nowhere, C=RZ") == false)
								reply(this.prefix + "[WARNING] " + servername + " does not have a correct issuer DN");
						}
						else
								reply(this.prefix + servername + " has non X509 certificate " + cert.getPublicKey() + " - " + cert.getType());
					}
					if (this.quiet == false)
						reply(this.prefix + "[" + servername + "] Global users: " + this.users + ", Servers: " + this.servers + ", Uptime: " + token[5] + " days " + token[7]);
					s.shutdown();
					break;
				}
			}
		}
		catch (NoRouteToHostException ex)
		{
			reply(this.prefix + "Unable to connect to " + this.server.getName() + ", no route to host");
		}
		catch (SocketTimeoutException ex)
		{
			reply(this.prefix + "Unable to connect to " + this.server.getName() + ", connection timeout");
		}
		catch (IOException ex)
		{
			reply(this.prefix + "Unable to connect to " + this.server.getName());
		}
		catch (Exception ex)
		{
			reply(this.prefix + "[EXCEPTION] Unable to connect to " + this.server.getName() + ": " + ex);
			Logger.getGlobalLogger().log(ex);
		}
		finally
		{
			try
			{
				s.shutdown();
			}
			catch (Exception ex) { }
		}
	}
	*/
	
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
		
		if (Moo.conf.general.host != null)
			try
			{
				client.bind(new InetSocketAddress(Moo.conf.general.host, 0)).sync().await();
			}
			catch (InterruptedException ex) { }

		ChannelFuture future = client.connect(Moo.conf.general.server, Moo.conf.general.port);
		this.channel = future.channel();
	}
	
	public boolean isSsl()
	{
		return ssl;
	}
}
