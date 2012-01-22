package net.rizon.moo.servercontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

final class connectionSSH extends connection
{
	private static JSch jsch = new JSch();
	
	private Session session;
	private ChannelExec channel;
	private PrintStream shellStream;
	private BufferedReader reader;

	public connectionSSH(protocol proto)
	{
		super(proto);
		this.setPort(22);
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		try { this.reader.close(); }
		catch (IOException ex) { }
		this.shellStream.close();
		this.channel.disconnect();
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
	
	public void connect() throws Exception
	{
		try
		{
			this.session = jsch.getSession(this.getUser(), this.getHost());
			this.session.setPort(this.getPort());
			this.session.setPassword(this.getPassword());
			this.session.setConfig("StrictHostKeyChecking", "no");
			this.session.connect();
		}
		catch (JSchException e)
		{
			throw new Exception(e);
		}
	}
	
	public void execute(final String command) throws Exception
	{
		this.cleanUp();

		this.channel = (ChannelExec) this.session.openChannel("exec");
		this.shellStream = new PrintStream(this.channel.getOutputStream());
		this.reader = new BufferedReader(new InputStreamReader(this.channel.getInputStream()));
		this.channel.setCommand(command);
		this.channel.connect();
	}
	
	public String readLine()
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
	public protocolSSH()
	{
		super("SSH");
	}
	
	
	public connection createConnection()
	{
		return new connectionSSH(this);
	}
}