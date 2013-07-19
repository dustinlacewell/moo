package net.rizon.moo.servercontrol.protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import org.apache.commons.net.telnet.TelnetClient;

import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.protocol;
import net.rizon.moo.servercontrol.serverInfo;

final class connectionTelnet extends connection
{
	private TelnetClient client = null;
	private PrintStream shellStream = null;
	private BufferedReader reader = null;

	public connectionTelnet(protocol proto, serverInfo si)
	{
		super(proto, si);
	}
	
	@Override
	public void destroy()
	{
		super.destroy();

		try { this.client.disconnect(); }
		catch (Exception ex) { }
		
		try { this.shellStream.close(); }
		catch (Exception ex) { }
		
		try { this.reader.close(); }
		catch (Exception ex) { }
	}
	
	private void readUntil(final String[] patterns) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		for (char i; (i = (char) this.reader.read()) > 0;)
		{
			sb.append(i);
			for (final String pattern : patterns)
				if (sb.toString().endsWith(pattern))
					return;
		}
	}
	
	private static final String[] login_patterns = { "login:", "Username:" };
	private static final String[] password_patterns = { "Password:" };
	
	
	@Override
	public boolean isConnected()
	{
		return this.client != null && this.client.isConnected();
	}

	@Override
	public void connect() throws IOException
	{
		this.client = new TelnetClient();
		int port = this.getServerInfo().port;
		if (port == 0)
			port = 23;
		this.client.connect(this.getServerInfo().host, port);
		this.shellStream = new PrintStream(this.client.getOutputStream());
		this.reader = new BufferedReader(new InputStreamReader(this.client.getInputStream()));
		
		this.readUntil(login_patterns);
		this.execute(this.getServerInfo().user);
		
		this.readUntil(password_patterns);
		this.execute(this.getServerInfo().pass);
		
		try
		{
			Thread.sleep(1000);
		}
		catch (InterruptedException e) { }
	}

	@Override
	public void execute(String command) throws IOException
	{
		this.shellStream.println(command);
		this.shellStream.flush();
	}

	@Override
	public String readLine()
	{
		long last_recv = System.currentTimeMillis() / 1000L;
		
		try
		{
			while (last_recv + 30 > System.currentTimeMillis() / 1000L)
			{
				if (this.reader.ready())
				{
					this.reader.mark(512);
					char buf[] = new char[512];
					this.reader.read(buf, 0, 512);
					this.reader.reset();
					for (int i = 0; i < buf.length; ++i)
						if (buf[i] == '\n')
						{
							last_recv = System.currentTimeMillis() / 1000L;
							return this.reader.readLine();
						}
				}
				
				try
				{
					Thread.sleep(500);
				}
				catch (InterruptedException e) { }
			}
		}
		catch (IOException ex) { }
		
		return null;
	}
}

public class protocolTelnet extends protocol
{
	public protocolTelnet()
	{
		super("TELNET");
	}
	
	@Override
	public connection createConnection(serverInfo si)
	{
		return new connectionTelnet(this, si);
	}
}
