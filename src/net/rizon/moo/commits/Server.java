package net.rizon.moo.commits;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;
import net.rizon.moo.commits.api.bitbucket.Bitbucket;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

class Server extends Thread
{
	private static final Logger log = Logger.getLogger(Server.class.getName());
	private static final int maxPayload = 65535;
	
	private ServerSocket sock;
	
	private String ip;
	private int port;
	
	public Server(final String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	@Override
	public void run()
	{
		try
		{
			this.sock = new ServerSocket();
			this.sock.bind(new InetSocketAddress(this.ip, this.port));
			
			for (Socket client; (client = this.sock.accept()) != null;)
			{
				BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				
				try
				{
					String str;
					int len = -1;
	
					for (; (str = input.readLine()) != null; str = null)
						if (str.startsWith("Content-Length: "))
						{
							try
							{
								len = Integer.parseInt(str.substring(16));
							}
							catch (NumberFormatException ex)
							{
								log.log(Level.WARNING, "Invalid content length (" + str + ")");
								str = null;
								break;
							}
							
							if (len > maxPayload)
							{
								log.log(Level.WARNING, "Content length is huge! (" + len + " > " + maxPayload + ")");
								str = null;
								break;
							}
						}
						else if (str.isEmpty())
							break;
					
					if (str == null || len == -1)
						continue;
					
					char[] payload = new char[len];
					int bytesread = 0;
					while (bytesread < len)
					{
						int i = input.read(payload, bytesread, len - bytesread);
						if (i <= 0)
							throw new IOException("End of stream");
						bytesread += i;
					}
					
					str = new String(payload, 0, len);
					if (str.startsWith("payload="))
					{
						final String json = URLDecoder.decode(str.substring(8), "UTF-8");
						try
						{
							Push p = new Gson().fromJson(json, Bitbucket.class);
						
							for (Commit c : p.getCommits())
							{
								if (c.getMessage().length > 1)
								{
									Moo.privmsg(Moo.conf.getCommitsChannel(), "\2" + p.getProjectName() + "\2: \00303" + c.getAuthor() + "\003 \00307" + c.getBranch() + "\003:");
									for (final String msg : c.getMessage())
										Moo.privmsg(Moo.conf.getCommitsChannel(), msg);
								}
								else if (c.getMessage().length == 1)
									Moo.privmsg(Moo.conf.getCommitsChannel(), "\2" + p.getProjectName() + "\2: \00303" + c.getAuthor() + "\003 \00307" + c.getBranch() + "\003: " + c.getMessage()[0]);
							}
						}
						catch (JsonSyntaxException e)
						{
							log.log(Level.WARNING, "Exception while parsing json", e);
							log.log(Level.WARNING, "Payload: " + json);
							
							// Don't continue; we need to see the fail only once.
						}
					}
					
					output.write("HTTP/1.1 200 OK\r\n");
					output.write("Content-Type: text/html; charset=UTF-8\r\n");
					output.write("Server: moo\r\n");
					output.write("Connection: close\r\n");
					output.write("Content-Length: 2\r\n\r\nOK");
					output.flush();
				}
				catch (Exception ex)
				{
					log.log(ex);
				}
				finally
				{
					try { input.close(); } catch (IOException ex) { }
					try { output.close(); } catch (IOException ex) { }
					try { client.close(); } catch (IOException ex) { }
				}
			}
			
			this.sock.close();
		}
		catch (IOException ex)
		{
			log.log(ex);
		}
	}
	
	protected void shutdown()
	{
		try
		{
			this.sock.close();
		}
		catch (IOException e)
		{
			log.log(e);
		}
	}
}
