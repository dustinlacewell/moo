package net.rizon.moo.commands;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.security.cert.Certificate;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Random;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;
import net.rizon.moo.socket;
import net.rizon.moo.timer;

class scheck extends Thread
{
	private String server;
	private int port;
	private boolean ssl;
	private String source;
	private String target;
	private String users;
	private String servers;
	private boolean quiet;
	
	private static final Random rand = new Random();
	private static final String getRandom()
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

	public scheck(final String server, final String source, final String target, boolean ssl, int port, boolean quiet)
	{
		this.server = server;
		this.source = source;
		this.target = target;
		this.ssl = ssl;
		this.port = port;
		this.quiet = quiet;
	}
	
	@Override
	public void run()
	{
		socket s = null;
		
		try
		{
			if (this.ssl == true)
				s = socket.createSSL();
			else
				s = socket.create();
			if (this.quiet == false)
			{
				if (this.ssl == true || this.port != 6667)
					moo.reply(this.source, this.target, "[SCHECK] Connecting to " + this.server + ":" + (this.ssl == true ? "+" : "") + this.port);
				else
					moo.reply(this.source, this.target, "[SCHECK] Connecting to " + this.server + "...");
			}
			s.connect(this.server, this.port, 15000);

			s.write("USER " + moo.conf.getIdent() + " . . :" + moo.conf.getRealname());
			s.write("NICK " + moo.conf.getNick() + "-" + getRandom());
			
			for (String in; (in = s.read()) != null;)
			{
				String[] token = in.split(" ");
				
				if (token.length > 11 && token[1].equals("251"))
				{
					this.servers = token[11];
				}
				else if (token.length > 8 && token[1].equals("266"))
				{
					this.users = token[8].replace(",", "");
					s.write("STATS u");
				}
				else if (token.length > 7 && token[1].equals("242"))
				{
					final String servername = token[0].substring(1);
					
					if (this.ssl == true)
					{
						Certificate[] certs = s.getSSLSocket().getSession().getPeerCertificates();
						for (Certificate cert : certs)
						{
							if (cert instanceof X509Certificate)
							{
								X509Certificate x509 = (X509Certificate) cert;
								
								try
								{
									x509.checkValidity();
								}
								catch (CertificateExpiredException e)
								{
									moo.reply(this.source, this.target, "[SCHECK] [WARNING] " + servername + " has EXPIRED X509 SSL certificate!");
								}
								catch (CertificateNotYetValidException e)
								{
									moo.reply(this.source, this.target, "[SCHECK] [WARNING] " + servername + " has a NOT VALID YET X509 SSL certificate!");
								}
								
								final String issuerDn = x509.getIssuerDN().getName();
								if (this.quiet == false)
									moo.reply(this.source, this.target, "[SCHECK] " + servername + " has X509 certificate " + issuerDn);
								if (issuerDn.contains("O=Rizon IRC Network, CN=irc.rizon.net, L=Rizon, ST=Nowhere, C=RZ") == false)
									moo.reply(this.source, this.target, "[SCHECK] [WARNING] " + servername + " does not have a correct issuer DN");
							}
							else
									moo.reply(this.source, this.target, "[SCHECK] " + servername + " has non X509 certificate " + cert.getPublicKey() + " - " + cert.getType());
						}
					}
					if (this.quiet == false)
						moo.reply(this.source, this.target, "[SCHECK] [" + servername + "] Global users: " + this.users + ", Servers: " + this.servers + ", Uptime: " + token[5] + " days " + token[7]);
					s.shutdown();
					break;
				}
			}
		}
		catch (NoRouteToHostException ex)
		{
			moo.reply(this.source, this.target, "[SCHECK] Unable to connect to " + this.server + ", no route to host");
		}
		catch (SocketTimeoutException ex)
		{
			moo.reply(this.source, this.target, "[SCHECK] Unable to connect to " + this.server + ", connection timeout");
		}
		catch (IOException ex)
		{
			moo.reply(this.source, this.target, "[SCHECK] Unable to connect to " + this.server);
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
}

class scheckTimer extends timer
{
	protected static final int delay = 70;
	
	private String server;
	private int port;
	private boolean ssl;
	private String source;
	private String target;
	
	public scheckTimer(int delay, final String server, final String source, final String target, boolean ssl, int port)
	{
		super(delay * scheckTimer.delay, false);
		
		this.server = server;
		this.source = source;
		this.target = target;
		this.ssl = ssl;
		this.port = port;
	}

	@Override
	public void run(Date now)
	{
		scheck check = new scheck(this.server, this.source, this.target, this.ssl, this.port, true);
		check.start();
	}
}

class scheckEndTimer extends timer
{
	private String source;
	private String target;
	
	public scheckEndTimer(int delay, final String source, final String target)
	{
		super(delay * scheckTimer.delay, false);
		
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void run(Date now)
	{
		moo.reply(this.source, this.target, "[SCHECK] All server checks completed.");
	}
}

public class commandScheck extends command
{
	public commandScheck(mpackage pkg)
	{
		super(pkg, "!SCHECK", "Check if a server is online");
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !SCHECK <server> [+port]");
		moo.notice(source, "Attempts to connect to the given server. If no port is given, 6667 is assumed.");
		moo.notice(source, "If port is prefixed with a +, SSL is used.");
		moo.notice(source, "Once a connection is established, the global user count, server count and uptime will be shown.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
			moo.reply(source, target, "Syntax: !scheck <server> [port]");
		else
		{
			server serv = server.findServer(params[1]);
			if (serv == null && params[1].equalsIgnoreCase("ALL") == false)
				moo.reply(source, target, "[SCHECK] Server " + params[1] + " not found");
			else
			{
				int port = 6667;
				boolean ssl = false;
				if (params.length > 2)
				{
					String port_str = params[2];
					if (port_str.startsWith("+"))
					{
						port_str = port_str.substring(1);
						ssl = true;
					}
					
					try
					{
						port = Integer.parseInt(port_str);
						if (port <= 0 || port > 65535)
							throw new NumberFormatException("Invalid port range");
					}
					catch (NumberFormatException ex)
					{
					}
				}
				
				if (params[1].equalsIgnoreCase("ALL") == false)
				{
					scheck check = new scheck(serv.getName(), source, target, ssl, port, false);
					check.start();
				}
				else
				{
					int delay = 0;
					
					for (server s : server.getServers())
					{
						if (s.isHub() || s.isServices())
							continue;
						
						new scheckTimer(delay++, s.getName(), source, target, ssl, port).start();
					}
					
					new scheckEndTimer(delay + 1, source, target);
					
					moo.reply(source, target, "[SCHECK] Queued " + delay + " checks in the next " + (delay * scheckTimer.delay) + " seconds");
				}
			}
		}
	}
}
