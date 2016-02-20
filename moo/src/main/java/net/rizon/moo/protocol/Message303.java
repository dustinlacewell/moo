package net.rizon.moo.protocol;

import com.google.inject.Inject;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.io.IRCMessage;
import org.slf4j.Logger;

public class Message303 extends Message
{
	@Inject
	private static Logger logger;

	public Message303()
	{
		super("303");
	}

	@Override
	public void run(IRCMessage message)
	{
		for (final String nick : message.getParams()[1].split(" "))
			if (nick.equalsIgnoreCase("GeoServ"))
			{
				logger.info("GeoServ has come back! Changing akillserv back to GeoServ");
				
				Moo.akillServ = "GeoServ";
			}
	}
}