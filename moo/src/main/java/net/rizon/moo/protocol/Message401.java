package net.rizon.moo.protocol;

import java.util.Date;
import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Timer;

class geoChecker extends Timer
{
	public geoChecker()
	{
		super(10, true);
	}

	@Override
	public void run(Date now)
	{
		if (Moo.akillServ.equalsIgnoreCase("GeoServ"))
		{
			this.setRepeating(false);
			return;
		}

		Moo.write("ISON", "GeoServ");
	}
}

public class Message401 extends Message
{
	private static final Logger log = Logger.getLogger(Message401.class.getName());

	public Message401()
	{
		super("401");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equalsIgnoreCase("GeoServ"))
		{
			Moo.akillServ = "OperServ";

			log.log(Level.INFO, "GeoServ has gone away! Changing akillserv to OperServ");
			new geoChecker().start();
		}
		else if (message[1].equalsIgnoreCase("OperServ"))
			Moo.akillServ = "GeoServ";
	}
}