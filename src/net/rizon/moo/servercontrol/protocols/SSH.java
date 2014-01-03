package net.rizon.moo.servercontrol.protocols;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.Protocol;
import net.rizon.moo.servercontrol.ServerInfo;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

final class connectionSSH extends Connection
{
	private JSch jsch;
	private Session session = null;
	private Channel channel = null;
	private PrintStream shellStream = null;
	private BufferedReader reader = null;

	public connectionSSH(Protocol proto, ServerInfo si, JSch jsch)
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
			ChannelExec ce = (ChannelExec) this.session.openChannel("exec");
			this.channel = ce;
			this.shellStream = new PrintStream(this.channel.getOutputStream());
			this.reader = new BufferedReader(new InputStreamReader(this.channel.getInputStream()));
			ce.setCommand(command);
			this.channel.connect();
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
	}
	
	private ChannelSftp openSftpChannel() throws IOException, JSchException
	{
		this.cleanUp();
		
		ChannelSftp cs = (ChannelSftp) this.session.openChannel("sftp");
		cs.connect();
		return cs;
	}
	
	@Override
	public synchronized void upload(File file, String dest) throws IOException
	{
		try
		{
			ChannelSftp cs = this.openSftpChannel();
			this.channel = cs;
			cs.put(new FileInputStream(file), dest);
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
		catch (SftpException e)
		{
			throw new IOException(e);
		}
	}
	
	@Override
	public synchronized void download(String file, String dest) throws IOException
	{
		try
		{
			ChannelSftp cs = this.openSftpChannel();
			this.channel = cs;
			InputStream is = cs.get(file);
			
			FileOutputStream fi = new FileOutputStream(dest);
			for (int i; (i = is.read()) != -1;)
				fi.write(i);
			
			is.close();
			fi.close();
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
		catch (SftpException e)
		{
			throw new IOException(e);
		}
	}
	
	@Override
	public synchronized void remove(String file) throws IOException
	{
		try
		{
			ChannelSftp cs = this.openSftpChannel();
			this.channel = cs;
			cs.rm(file);
		}
		catch (JSchException e)
		{
			throw new IOException(e);
		}
		catch (SftpException e)
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

public class SSH extends Protocol
{
	private static final Logger log = Logger.getLogger(SSH.class.getName());
	
	private static JSch jsch = new JSch();
	
	public SSH()
	{
		super("SSH");
		
		for (final String key : Moo.conf.getList("ssh_key_paths"))
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
	public Connection createConnection(ServerInfo si)
	{
		return new connectionSSH(this, si, jsch);
	}
}
