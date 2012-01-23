package net.rizon.moo.servercontrol.protocols;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.protocol;

final class connectionFTP extends connection
{
	private FTPClient client = null;
	private LinkedList<String> replies = new LinkedList<String>();

	public connectionFTP(protocol proto)
	{
		super(proto);
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
		int port = this.getServerInfo().port;
		if (port == 0)
			port = 21;
		this.client.connect(this.getServerInfo().host, port);
		this.client.login(this.getServerInfo().user, this.getServerInfo().pass);
		
		if (FTPReply.isPositiveCompletion(this.client.getReplyCode()) == false)
			throw new IOException("Unable to login");
	}

	@Override
	public void execute(String command) throws IOException
	{
		int sp = command.indexOf(' ');
		String params = null;
		if (sp != -1)
		{
			params = command.substring(sp + 1);
			command = command.substring(0, 1);
		}
		
		if (command.equalsIgnoreCase("LS"))
		{
			FTPFile[] files = this.client.listFiles(params);
			for (FTPFile file : files)
				this.replies.addLast(file.getName());
		}
		else if (command.equalsIgnoreCase("PUT"))
		{
			File f = new File(params);
			FileInputStream fis = new FileInputStream(f);
			if (this.client.storeFile(f.getName(), fis))
				this.replies.addLast("Successfully uploaded file " + f.getName());
			else
				this.replies.addLast("Error uploading file " + f.getName());
			fis.close();
		}
		else if (this.client.doCommand(command, params) == false)
			throw new IOException("Error executing FTP command");
	}

	private String last_return = null;
	@Override
	public String readLine()
	{
		if (this.replies.isEmpty() == false)
			return this.replies.removeFirst();
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
