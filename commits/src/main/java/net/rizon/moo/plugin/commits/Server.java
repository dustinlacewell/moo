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
import net.rizon.moo.Moo;
import net.rizon.moo.plugin.commits.api.gitlab.GitLab;
import net.rizon.moo.plugin.commits.api.gitlab.ObjectAttributes;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Server extends Thread
{
	protected static final Logger logger = LoggerFactory.getLogger(commits.class);
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
			logger.error("Unable to shutdown commits listener", ex);
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
								logger.warn("Invalid content length ({})", str);
								str = null;
								break;
							}

							if (len > maxPayload)
							{
								logger.warn("Content length is huge! ({} > {})", len, maxPayload);
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

							if (!attrs.getAction().equals("update"))
							{
								// Update events are annoying, they get sent together with close / open.
								Moo.privmsgAll(commits.conf.channels, "\2" + projectName + "\2: \00303" + attrs.getAction() + " issue\003 by \00303" + p.getUser().name + "\003: " + attrs.getTitle() + " \u001f" + attrs.getUrl() + "\u000f");
							}
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
						else if (p.getObjectKind() != null && p.getObjectKind().equals("note"))
						{
							ObjectAttributes attrs = p.getObjectAttributes();
							if (attrs.getNotableType().equals("Commit"))
							{
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303" + p.getUser().name + "\003 commented on commit \00307" + attrs.getCommitId() + "\003");
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303Comment\003: " + attrs.getNote() + " \u001f" + attrs.getUrl() + "\u000f");
							}
							else if (attrs.getNotableType().equals("MergeRequest"))
							{
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303" + p.getUser().name + "\003 commented on merge request \00307!" + p.getMergeRequest().iid + "\003");
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303Comment\003: " + attrs.getNote() + " \u001f" + attrs.getUrl() + "\u000f");
							}
							else if (attrs.getNotableType().equals("Issue"))
							{
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303" + p.getUser().name + "\003 commented on issue \00307#" + p.getIssue().iid + "\003 " + p.getIssue().title);
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303Comment\003: " + attrs.getNote() + " \u001f" + attrs.getUrl() + "\u000f");
							}
							else if (attrs.getNotableType().equals("Snippet"))
							{
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303" + p.getUser().name + "\003 commented on snippet \00307#" + attrs.getNotableId() + "\003");
								Moo.privmsgAll(commits.conf.channels, "\2" + p.getProjectName() + "\2: \00303Comment\003: " + attrs.getNote() + " \u001f" + attrs.getUrl() + "\u000f");
							}
						}
						else if (p.getObjectKind() != null && p.getObjectKind().equals("merge_request"))
						{
							ObjectAttributes attrs = p.getObjectAttributes();
							if (attrs.getAction().equals("open"))
							{
								Moo.privmsgAll(commits.conf.channels, "\2" + attrs.getTarget().name + "\2: \00303" + p.getUser().name + "\003 opened merge request \00307!" + attrs.getIid() + "\003");
								Moo.privmsgAll(commits.conf.channels, "\2" + attrs.getTarget().name + "\2:" + " \u001f" + attrs.getUrl() + "\u000f");
							}
							if (attrs.getAction().equals("close"))
							{
								Moo.privmsgAll(commits.conf.channels, "\2" + attrs.getTarget().name + "\2: \00303" + p.getUser().name + "\003 closed merge request \00307!" + attrs.getIid() + "\003");
								Moo.privmsgAll(commits.conf.channels, "\2" + attrs.getTarget().name + "\2:" + " \u001f" + attrs.getUrl() + "\u000f");
							}
						}
						else
						{
							logger.warn("Unknown GitLab event, payload was: {}", json);
						}

					}
					catch (JsonSyntaxException e)
					{
						logger.warn("Exception while parsing json", e);
						logger.warn("Payload: {}", json);

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
					logger.warn("Unable to process commits message", ex);
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
			logger.warn("Error in commits server", ex);
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
			logger.error("Unable to shutdown commits server", e);
		}
	}
}
