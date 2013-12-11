package net.rizon.moo.tinc;

import java.util.LinkedList;

public class Host
{
	public String name = "", address = "";
	public int port;
	public LinkedList<String> subnets = new LinkedList<String>();
	public String key = "";
	
	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof Host))
			return false;

		Host h = (Host) other;
	
		if (!this.name.equals(h.name))
			return false;
		if (!this.address.equals(h.address))
			return false;
		if (this.port != h.port)
			return false;
		if (!this.subnets.equals(h.subnets))
			return false;
		if (!this.key.equals(h.key))
			return false;

		return true;
	}
}