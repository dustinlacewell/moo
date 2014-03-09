package net.rizon.moo.protocol;

import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;

public class Message303 extends Message
{
	private static final Logger log = Logger.getLogger(Message303.class.getName());
	
	public Message303()
	{
		super("303");
	}

	@Override
	public void run(String source, String[] message)
	{
		for (final String nick : message[1].split(" "))
			if (nick.equalsIgnoreCase("GeoServ"))
			{
				log.log(Level.INFO, "GeoServ has come back! Changing akillserv back to GeoServ");
				Moo.akillServ = "GeoServ";
			}
	}
}