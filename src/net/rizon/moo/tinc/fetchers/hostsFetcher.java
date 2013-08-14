package net.rizon.moo.tinc.fetchers;

import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.process;
import net.rizon.moo.tinc.host;
import net.rizon.moo.tinc.node;
import net.rizon.moo.tinc.tinc;


public class hostsFetcher extends process
{
	public static process create(node n, connection con, String source, String target)
	{
		String cmd = "ls -l " + tinc.tincBase + "/" + n.getLayer().getName() + "/hosts";
		return new hostsFetcher(n, con, cmd, source, target);
	}
	
	private node n;
	private String source, target;
	
	public hostsFetcher(node n, connection c, String commands, String source, String target)
	{
		super(c, commands);
		
		n.hosts.clear();
		
		this.n = n;
		this.source = source;
		this.target = target;
	}
		
	private void fetchHosts(host h)
	{
		connection con = connection.findOrCreateConncetion(n.getServer().getServerInfo());
		process p = hostFetcher.create(n, con, h, source, target);
		p.start();
	}
	
	@Override
	public void onLine(String in)
	{
		while (in.indexOf("  ") != -1)
			in = in.replaceAll("  ", " ");

		String[] tokens = in.split(" ");
		if (tokens.length <= 5)
			return;

		host h = new host();
		h.name = tokens[tokens.length - 1];
		n.hosts.add(h);
		
		this.fetchHosts(h);
	}
}
