package net.rizon.moo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class socket extends Socket
{
	private PrintWriter out;
	private BufferedReader in;

	public socket()
	{
	}
	
	public void connect(final String addr, int port) throws IOException
	{
		this.connect(addr, port, 0);
	}
	
	public void connect(final String addr, int port, int timeout) throws IOException
	{
		this.connect(new InetSocketAddress(addr, port), timeout);
		this.out = new PrintWriter(this.getOutputStream(), true);
		this.in = new BufferedReader(new InputStreamReader(this.getInputStream()));
	}
	
	public void shutdown()
	{
		try { this.out.close(); }
		catch (Exception ex) { }
		
		try { this.in.close(); }
		catch (Exception ex) { }
		
		try { this.clone(); }
		catch (Exception ex) { }
	}
	
	public void write(final String buf)
	{
		if (moo.conf.getDebug() > 0)
			System.out.println("-> " + buf);
		this.out.println(buf);
	}
	
	public final String read() throws IOException
	{
		String in = this.in.readLine();
		if (moo.conf.getDebug() > 0)
			System.out.println("<- " + in);
		return in;
	}
	
	public void privmsg(String target, final String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		this.write("PRIVMSG " + target + " :" + buffer);
	}
	
	public void notice(String target, final String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		this.write("NOTICE " + target + " :" + buffer);
	}
	
	public void join(String target)
	{
		this.write("JOIN " + target);
	}
}
