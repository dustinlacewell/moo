package net.rizon.moo.servercontrol.protocols;

import java.io.IOException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;

import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.protocol;

final class connectionFTP extends connection
{
	private FTPClient client = null;

	public connectionFTP(protocol proto)
	{
		super(proto);
		this.setPort(21);
	}
	
	@Override
	public void destroy()
	{
		super.destroy();
		
		try { this.client.logout(); }
		catch (Exception ex) { }
		try { this.client.disconnect(); }
		catch (Exception ex) { }
	}

	@Override
	public boolean isConnected()
	{
		return this.client != null && this.client.isConnected();
	}

	@Override
	public void connect() throws IOException
	{
		this.client = new FTPClient();
		this.client.connect(this.getHost(), this.getPort());
		this.client.login(this.getUser(), this.getPassword());
		
		if (FTPReply.isPositiveCompletion(this.client.getReplyCode()) == false)
			throw new IOException("Unable to login");
	}

	@Override
	public void execute(String command) throws IOException
	{
		if (this.client.doCommand(command, null) == false)
			throw new IOException("Error executing FTP command");
	}

	private String last_return = null;
	@Override
	public String readLine()
	{
		if (this.client.getReplyString() != last_return)
		{
			last_return = this.client.getReplyString();
			return last_return;
		}
		return null;
	}
}

public class protocolFTP extends protocol
{
	public protocolFTP()
	{
		super("FTP");
	}
	
	@Override
	public connection createConnection()
	{
		return new connectionFTP(this);
	}
}
