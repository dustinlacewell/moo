package net.rizon.moo.tinc.fetchers;

import net.rizon.moo.moo;
import net.rizon.moo.servercontrol.connection;
import net.rizon.moo.servercontrol.process;
import net.rizon.moo.tinc.node;
import net.rizon.moo.tinc.tinc;

public class configurationFetcher extends process
{
	public static process create(node n, connection con, String source, String target)
	{
		String cmd = "cat " + tinc.tincBase + "/" + n.getLayer().getName() + "/tinc.conf";
		return new configurationFetcher(n, con, cmd, source, target);
	}

	static String warned = "";
	
	private node node;
	private connection con;
	private String source, target;

	public configurationFetcher(node n, connection c, String commands, String source, String target)
	{
		super(c, commands);
		
		n.name = "";
		n.pubAddress = "";
		n.connectTo.clear();
		
		this.node = n;
		this.con = c;
		this.source = source;
		this.target = target;
	}
	
	@Override
	public void onLine(String in)
	{
		while (in.indexOf("  ") != -1)
			in = in.replaceAll("  ", " ");
		in = in.replaceAll("	", " "); // tab

		String[] tokens = in.split(" ");
		
		if (tokens[0].equals("Name"))
			this.node.name = tokens[1];
		else if (tokens[0].equals("BindToAddress"))
			this.node.pubAddress = tokens[1];
		else if (tokens[0].equals("ConnectTo"))
			this.node.connectTo.add(tokens[1]);
	}
	
	@Override
	public void onFinish()
	{
		if (this.node.name.isEmpty())
			return;
		
		moo.reply(source, target, "[" + this.con.getServerInfo().name + "] Processed tinc configuration for " + this.node.name + " on layer " + this.node.getLayer().getName()); 
	}

	@Override
	public void onError(Exception ex)
	{
		if (warned.equals(this.con.getServerInfo().name))
			return;
		
		configurationFetcher.warned = this.con.getServerInfo().name;
		moo.reply(source, target, "[" + this.con.getServerInfo().name + "] Error: " + ex.getMessage());
	}
}
