package net.rizon.moo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.logging.Level;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

class TrustingSSLSocketFactory extends SSLSocketFactory
{
	public static TrustingSSLSocketFactory sslSocketFactory = null;
	
	static
	{
		try
		{
			sslSocketFactory = new TrustingSSLSocketFactory();
		}
		catch (SSLException ex)
		{
			logger.getGlobalLogger().log(ex);
		}
	}
	
	private class TrustingX509TrustManager implements X509TrustManager
	{
		@Override
		public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
		{
		}

		@Override
		public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException
		{	
		}

		@Override
		public X509Certificate[] getAcceptedIssuers()
		{
			return new X509Certificate[0];
		}
	}
	
	private SSLSocketFactory factory;
	private String[] ciphers;
	
	public TrustingSSLSocketFactory() throws SSLException
	{
		try
		{
			SSLContext context = SSLContext.getInstance("SSlv3");
			context.init(null, new TrustManager[] { new TrustingX509TrustManager()}, null);
			this.factory = context.getSocketFactory();
			this.ciphers = this.factory.getDefaultCipherSuites();
		}
		catch (NoSuchAlgorithmException ex)
		{
			throw new SSLException("Unable to initialize the SSL context");
		}
		catch (KeyManagementException ex)
		{
			throw new SSLException("Unable to register a trust manger");
		}
	}
	
	private Socket prepare(Socket base)
	{
		SSLSocket basessl = (SSLSocket) base;
		basessl.setEnabledCipherSuites(this.ciphers);
		return basessl;
	}

	@Override
	public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException
	{
		return this.prepare(this.factory.createSocket(s, host, port, autoClose));
	}

	@Override
	public String[] getDefaultCipherSuites()
	{
		return this.ciphers;
	}

	@Override
	public String[] getSupportedCipherSuites()
	{
		return this.ciphers;
	}
	
	@Override
	public Socket createSocket() throws IOException
	{
		return this.prepare(this.factory.createSocket());
	}

	@Override
	public Socket createSocket(String host, int port) throws IOException, UnknownHostException
	{
		return this.prepare(this.factory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(InetAddress host, int port) throws IOException
	{
		return this.prepare(this.factory.createSocket(host, port));
	}

	@Override
	public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException
	{
		return this.prepare(this.factory.createSocket(host, port, localHost, localPort));
	}

	@Override
	public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException
	{
		return this.prepare(this.factory.createSocket(address, port, localAddress, localPort));
	}
}

public class socket
{
	private static final logger log = logger.getLogger(socket.class.getName());

	private Socket sock;
	private SSLSocket sslsock;
	private PrintWriter out;
	private BufferedReader in;

	private socket()
	{
	}
	
	public static socket create()
	{
		socket s = new socket();
		s.sock = new Socket();
		s.sslsock = null;
		return s;
	}
	
	public static socket createSSL() throws IOException
	{
		socket s = new socket();
		s.sock = TrustingSSLSocketFactory.sslSocketFactory.createSocket();
		s.sslsock = (SSLSocket) s.sock;
		return s;
	}
	
	public Socket getSocket()
	{
		return this.sock;
	}
	
	public SSLSocket getSSLSocket()
	{
		return this.sslsock;
	}
	
	public void connect(final String addr, int port) throws IOException
	{
		this.connect(addr, port, 0, false);
	}
	
	public void connect(final String addr, int port, int timeout) throws IOException
	{
		this.connect(addr,port, 0, false);
	}
	
	public void connect(final String addr, int port, int timeout, boolean use_v6) throws IOException
	{
		InetSocketAddress target = null;
		for (InetAddress ia : InetAddress.getAllByName(addr))
		{
			if ((!use_v6 && ia.getAddress().length != 4) ||
					(use_v6 && ia.getAddress().length != 16))
				continue;
			
			target = new InetSocketAddress(ia, port);
			break;
		}
		
		if (target == null)
			throw new UnknownHostException();
		
		this.sock.connect(target, timeout);
		this.out = new PrintWriter(this.sock.getOutputStream(), true);
		this.in = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
	}
	
	public void shutdown()
	{
		try { this.out.close(); }
		catch (Exception ex) { }
		
		try { this.in.close(); }
		catch (Exception ex) { }
		
		try { this.sock.close(); }
		catch (Exception ex) { }
	}
	
	public void write(final String buf)
	{
		log.log(Level.FINE, "-> " + buf);
		this.out.println(buf);
	}
	
	public final String read() throws IOException
	{
		String in = this.in.readLine();
		log.log(Level.FINE, "<- " + in);
		return in;
	}
}
