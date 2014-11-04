package net.rizon.moo.plugin.commits;

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
import net.rizon.moo.plugin.commits.api.gitlab.GitLab;
import net.rizon.moo.plugin.commits.api.gitlab.ObjectAttributes;

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
	
	public void stopServer()
	{
		try
		{
			this.sock.close();
		}
		catch (Exception ex)
		{
			log.log(Level.SEVERE, "Unable to shutdown commits listener");
		}
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

					final String json;
					if (str.startsWith("payload="))
					{
						str = str.substring(8);
						json = URLDecoder.decode(str, "UTF-8");
					}
					else
						json = str;

					try
					{
						GitLab p = new Gson().fromJson(json, GitLab.class);

						if (p.getObjectKind() != null && p.getObjectKind().equals("issue"))
						{
							ObjectAttributes attrs = p.getObjectAttributes();

							/* 
							 * very sadly gitlab doesn't provide this info, although
							 * we can try to guess it from the URL, although this still
							 * doesn't really give us the name of the person who opened
							 * the issue.
							 * 
							 * this is kind of bad.
							 */
							String[] parts = attrs.getUrl().split("/");
							String projectName = parts[4];

							Moo.privmsgAll(commits.conf.channels, "\2" + projectName + "\2: \00303" + attrs.getState() + " issue\003: " + attrs.getTitle() + " \u001f" + attrs.getUrl() + "\u000f");
						}
						else if (p.getCommits() != null)
						{
							if (p.getPusher() != null)
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303" + p.getPusher() + "\003 pushed \00307" + p.getCommits().size() + "\003 commit" + (p.getCommits().size() == 1 ? "" : "s") + " to \00307" + p.getBranch() + "\003");
							
							for (Commit c : p.getCommits())
							{
								String branch = c.getBranch() != null ? c.getBranch() : p.getBranch();
								if (c.getMessage().length > 1)
								{
									Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303" + c.getAuthor() + "\003 \00307" + branch + "\003:");
									for (final String msg : c.getMessage())
										Moo.privmsgAll(commits.conf.channels, msg);
								}
								else if (c.getMessage().length == 1)
									Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303" + c.getAuthor() + "\003 \00307" + branch + "\003: " + c.getMessage()[0] + " \u001f" + c.getUrl() + "\u000f");
							}
						}
						else
						{
							log.log(Level.WARNING, "Unknown GitLab event, payload was: " + json);
						}
						
					}
					catch (JsonSyntaxException e)
					{
						log.log(Level.WARNING, "Exception while parsing json", e);
						log.log(Level.WARNING, "Payload: " + json);
							
						// Don't continue; we need to see the fail only once.
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
