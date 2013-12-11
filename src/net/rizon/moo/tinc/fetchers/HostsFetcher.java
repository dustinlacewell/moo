package net.rizon.moo.tinc.fetchers;

import net.rizon.moo.servercontrol.Command;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.Process;
import net.rizon.moo.tinc.Host;
import net.rizon.moo.tinc.Node;
import net.rizon.moo.tinc.tinc;


public class HostsFetcher extends Command
{
	public static Process create(Node n, Connection con, String source, String target)
	{
		String cmd = "ls -l " + tinc.tincBase + "/" + n.getLayer().getName() + "/hosts";
		return new HostsFetcher(n, con, cmd, source, target);
	}
	
	private Node n;
	private String source, target;
	
	public HostsFetcher(Node n, Connection c, String commands, String source, String target)
	{
		super(c, commands);
		
		n.hosts.clear();
		
		this.n = n;
		this.source = source;
		this.target = target;
	}
		
	private void fetchHosts(Host h)
	{
		Connection con = Connection.findOrCreateConncetion(n.getServer().getServerInfo());
		Process p = HostFetcher.create(n, con, h, source, target);
		p.start();
	}
	
	@Override
	public void onLine(String in)
	{
		while (in.indexOf("  ") != -1)
			in = in.replaceAll("  ", " ");
		in = in.replaceAll("	", " "); // tab

		String[] tokens = in.split(" ");
		if (tokens.length <= 5)
			return;

		Host h = new Host();
		h.name = tokens[tokens.length - 1];
		n.hosts.add(h);
		
		this.fetchHosts(h);
	}
}
