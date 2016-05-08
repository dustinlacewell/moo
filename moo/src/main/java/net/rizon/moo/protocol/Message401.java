package net.rizon.moo.protocol;

import com.google.inject.Inject;
import java.util.concurrent.TimeUnit;

import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Protocol;
import org.slf4j.Logger;

class GeoChecker implements Runnable
{
	private final Protocol protocol;

	@Inject
	public GeoChecker(Protocol protocol)
	{
		this.protocol = protocol;
	}
		
	@Override
	public void run()
	{
		if (Moo.akillServ.equalsIgnoreCase("GeoServ"))
			return;

		protocol.write("ISON", "GeoServ");
		Moo.schedule(this, 10, TimeUnit.SECONDS);
	}
}

public class Message401 extends Message
{
	@Inject
	private static Logger logger;

	public Message401()
	{
		super("401");
	}

	@Override
	public void run(IRCMessage message)
	{
		if (message.getParams()[1].equalsIgnoreCase("GeoServ"))
		{
			Moo.akillServ = "OperServ";

			logger.info("GeoServ has gone away! Changing akillserv to OperServ");

			Moo.schedule(Moo.injector.getInstance(GeoChecker.class), 10, TimeUnit.SECONDS);
		}
		else if (message.getParams()[1].equalsIgnoreCase("OperServ"))
		{
			Moo.akillServ = "GeoServ";
		}
	}
}