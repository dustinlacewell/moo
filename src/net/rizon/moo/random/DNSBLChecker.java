package net.rizon.moo.random;

import java.net.InetAddress;
import java.net.UnknownHostException;

import net.rizon.moo.Moo;

class DNSBLChecker extends Thread
{
	private String bl;
	private NickData nd;
	
	protected DNSBLChecker(final String dnsbl, NickData nd)
	{
		this.bl = dnsbl;
		this.nd = nd;
	}
	
	@Override
	public void run()
	{
		final String ip = this.nd.ip;
		final String[] parts = ip.split("\\.");
		if (parts.length != 4)
			return;
		
		final String reverse_ip = parts[3] + "." + parts[2] + "." + parts[1] + "." + parts[0];
		
		InetAddress in;
		try
		{
			in = InetAddress.getByName(reverse_ip + "." + this.bl);
		}
		catch (UnknownHostException ex)
		{
			return;
		}
		
		if (in == null || !in.getHostAddress().startsWith("127.0.0."))
			return;
		
		for (final String chan : Moo.conf.getList("spam_channels"))
			Moo.privmsg(chan, nd + " is listed in " + this.bl + " with address " + in.getHostAddress());
		
		DNSBL d = DNSBL.getList(this.bl);
		this.nd.addList(d);
	}
}