package net.rizon.moo.tinc.fetchers;

import net.rizon.moo.servercontrol.Command;
import net.rizon.moo.servercontrol.Connection;
import net.rizon.moo.servercontrol.Process;
import net.rizon.moo.tinc.Host;
import net.rizon.moo.tinc.Node;
import net.rizon.moo.tinc.tinc;

public class HostFetcher extends Command
{
	public static Process create(Node n, Connection con, Host h, String source, String target)
	{
		String cmd = "cat " + tinc.tincBase + "/" + n.getLayer().getName() + "/hosts/" + h.name;
		return new HostFetcher(n, con, cmd, h, source, target);
	}
	
	private boolean inKey;
	private Host h;
	
	public HostFetcher(Node n, Connection c, String commands, Host h, String source, String target)
	{
		super(c, commands);
		
		this.h = h;
	}
	
	@Override
	public void onLine(String in)
	{
		while (in.indexOf("  ") != -1)
			in = in.replaceAll("  ", " ");
		in = in.replaceAll("	", " "); // tab

		String[] tokens = in.split(" ");

		if (tokens[0].equals("Address"))
			h.address = tokens[2];
		else if (tokens[0].equals("Port"))
			h.port = Integer.parseInt(tokens[2]);
		else if (tokens[0].equals("Subnet"))
			h.subnets.add(tokens[2]);
		else if (tokens[0].equals("-----BEGIN RSA PUBLIC KEY-----"))
			inKey = true;
		else if (tokens[0].equals("-----END RSA PUBLIC KEY-----"))
			inKey = false;
		else if (inKey)
			h.key += in;
	}
}