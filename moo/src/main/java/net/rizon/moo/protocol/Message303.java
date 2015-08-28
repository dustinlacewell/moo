package net.rizon.moo.protocol;

import java.util.logging.Level;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.io.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Message303 extends Message
{
	private static final Logger logger = LoggerFactory.getLogger(Message303.class);

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
				logger.info("GeoServ has come back! Changing akillserv back to GeoServ");
				
				Moo.akillServ = "GeoServ";
			}
	}
}