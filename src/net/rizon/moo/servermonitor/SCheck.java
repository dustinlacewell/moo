package net.rizon.moo.servermonitor;

import java.io.IOException;
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

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.Server;
import net.rizon.moo.Socket;

class SCheck extends Thread
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

			s.write("USER " + Moo.conf.getString("ident") + " . . :" + Moo.conf.getString("realname"));
			s.write("NICK " + Moo.conf.getString("nick") + "-" + getRandom());
			
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
}