package net.rizon.moo.tinc.fetchers;

import net.rizon.moo.Moo;
import net.rizon.moo.servercontrol.Command;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.Process;
import net.rizon.moo.tinc.Node;
import net.rizon.moo.tinc.tinc;

public class ConfigurationFetcher extends Command
{
	public static Process create(Node n, Connection con, String source, String target)
	{
		String cmd = "cat " + tinc.tincBase + "/" + n.getLayer().getName() + "/tinc.conf";
		return new ConfigurationFetcher(n, con, cmd, source, target);
	}

	static String warned = "";
	
	private Node node;
	private Connection con;
	private String source, target;

	public ConfigurationFetcher(Node n, Connection c, String commands, String source, String target)
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
		
		Moo.reply(source, target, "[" + this.con.getServerInfo().name + "] Processed tinc configuration for " + this.node.name + " on layer " + this.node.getLayer().getName()); 
	}

	@Override
	public void onError(Exception ex)
	{
		if (warned.equals(this.con.getServerInfo().name))
			return;
		
		ConfigurationFetcher.warned = this.con.getServerInfo().name;
		Moo.reply(source, target, "[" + this.con.getServerInfo().name + "] Error: " + ex.getMessage());
	}
}
