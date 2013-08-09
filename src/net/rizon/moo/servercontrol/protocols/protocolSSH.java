package net.rizon.moo.servercontrol.protocols;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Level;

import net.rizon.moo.logger;
import net.rizon.moo.moo;
import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.protocol;
import net.rizon.moo.servercontrol.serverInfo;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

final class connectionSSH extends connection
{
	private JSch jsch;
	private Session session = null;
	private ChannelExec channel = null;
	private PrintStream shellStream = null;
	private BufferedReader reader = null;

	public connectionSSH(protocol proto, serverInfo si, JSch jsch)
	{
		super(proto, si);
		this.jsch = jsch;
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		this.cleanUp();
		this.session.disconnect();
	}
	
	private void cleanUp()
	{
		try { this.channel.disconnect(); }
		catch (Exception ex) { }
		
		try { this.shellStream.close(); }
		catch (Exception ex) { }
		
		try { this.reader.close(); } 
		catch (Exception ex) { }
	}
	
	@Override
	public boolean isConnected()
	{
		return this.session != null && this.session.isConnected();
	}
	
	@Override
	public synchronized void connect() throws IOException
	{
		try
		{
			this.session = this.jsch.getSession(this.getServerInfo().user, this.getServerInfo().host);
			int port = this.getServerInfo().port;
			if (port == 0)
				port = 22;
			this.session.setPort(port);
			this.session.setPassword(this.getServerInfo().pass);
			this.session.setConfig("StrictHostKeyChecking", "no");
			this.session.connect();
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
	}
	
	@Override
	public synchronized void execute(final String command) throws IOException
	{
		this.cleanUp();

		try
		{
			this.channel = (ChannelExec) this.session.openChannel("exec");
			this.shellStream = new PrintStream(this.channel.getOutputStream());
			this.reader = new BufferedReader(new InputStreamReader(this.channel.getInputStream()));
			this.channel.setCommand(command);
			this.channel.connect();
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
	}
	
	@Override
	public synchronized String readLine()
	{
		try
		{
			return this.reader.readLine();
		}
		catch (IOException ex)
		{
			return null;
		}
	}
}

public class protocolSSH extends protocol
{
	private static final logger log = logger.getLogger(protocolSSH.class.getName());
	
	private static JSch jsch = new JSch();
	
	public protocolSSH()
	{
		super("SSH");
		
		for (final String key : moo.conf.getSSHKeyPaths())
		{
			try
			{
				jsch.addIdentity(key);
			}
			catch (JSchException ex)
			{
				log.log(Level.WARNING, "Unable to load private key " + key, ex);
			}
		}
	}
	
	@Override
	public connection createConnection(serverInfo si)
	{
		return new connectionSSH(this, si, jsch);
	}
}