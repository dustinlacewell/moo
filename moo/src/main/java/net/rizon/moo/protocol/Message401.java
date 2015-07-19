package net.rizon.moo.protocol;

import io.netty.util.concurrent.ScheduledFuture;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;

class GeoChecker implements Runnable
{
	@Override
	public void run()
	{
		if (Moo.akillServ.equalsIgnoreCase("GeoServ"))
			return;

		Moo.write("ISON", "GeoServ");
		Moo.schedule(this, 10, TimeUnit.SECONDS);
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
			Moo.schedule(new GeoChecker(), 10, TimeUnit.SECONDS);
		}
		else if (message[1].equalsIgnoreCase("OperServ"))
			Moo.akillServ = "GeoServ";
	}
}