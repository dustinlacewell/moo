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

import net.rizon.moo.moo;
import net.rizon.moo.commits.api.bitbucket.bitbucket;

import com.google.gson.Gson;

class server extends Thread
{
	private String ip;
	private int port;
	
	public server(final String ip, int port)
	{
		this.ip = ip;
		this.port = port;
	}
	
	@Override
	public void run()
	{
		try
		{
			ServerSocket sock = new ServerSocket();
			sock.bind(new InetSocketAddress(this.ip, this.port));
			
			for (Socket client; (client = sock.accept()) != null;)
			{
				BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				
				for (String str; (str = input.readLine()) != null;)
					if (str.isEmpty())
						break;
				
				char[] payload = new char[4096];
				int len = input.read(payload);
				
				final String str = new String(payload, 0, len);
				if (str.startsWith("payload="))
				{
					final String json = URLDecoder.decode(str.substring(8), "UTF-8");
					try
					{
						push p = new Gson().fromJson(json, bitbucket.class);
					
						for (commit c : p.getCommits())
						{
							if (c.getMessage().length > 1)
							{
								moo.privmsg(moo.conf.getCommitsChannel(), "\2" + p.getProjectName() + "\2: \00303" + c.getAuthor() + "\003 \00307" + c.getBranch() + "\003:");
								for (final String msg : c.getMessage())
									moo.privmsg(moo.conf.getCommitsChannel(), msg);
							}
							else if (c.getMessage().length == 1)
								moo.privmsg(moo.conf.getCommitsChannel(), "\2" + p.getProjectName() + "\2: \00303" + c.getAuthor() + "\003 \00307" + c.getBranch() + "\003: " + c.getMessage()[0]);
						}
					}
					catch (Exception e)
					{
						System.out.println("Exception while parsing json caught. Backtrace:");
						e.printStackTrace();
						System.out.println("Payload:");
						System.out.println(URLDecoder.decode(str.substring(8), "UTF-8"));
						System.out.println("End of payload.");
						
						for (final String channel : moo.conf.getDevChannels())
							moo.privmsg(channel, "\002Error parsing JSON for commit!\002");
						
						// Don't continue; we need to see the fail only once.
					}
				}
				
				output.write("HTTP/1.1 200 OK\r\n");
				output.write("Content-Type: text/html; charset=UTF-8\r\n");
				output.write("Connection: close\r\n");
				output.write("Content-Length: 2\r\n\r\nOK");
				output.close();
				client.close();
			}
			
			sock.close();
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}