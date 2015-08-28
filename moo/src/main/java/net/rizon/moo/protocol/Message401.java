package net.rizon.moo.protocol;

import java.util.concurrent.TimeUnit;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.io.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(Message401.class);

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

			logger.info("GeoServ has gone away! Changing akillserv to OperServ");
			Moo.schedule(new GeoChecker(), 10, TimeUnit.SECONDS);
		}
		else if (message[1].equalsIgnoreCase("OperServ"))
			Moo.akillServ = "GeoServ";
	}
}