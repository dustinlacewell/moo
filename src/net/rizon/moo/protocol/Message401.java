package net.rizon.moo.protocol;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;

public class Message401 extends Message
{
	public Message401()
	{
		super("401");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equalsIgnoreCase("GeoServ"))
			Moo.akillServ = "OperServ";
		else if (message[1].equalsIgnoreCase("OperServ"))
			Moo.akillServ = "GeoServ";
	}
}