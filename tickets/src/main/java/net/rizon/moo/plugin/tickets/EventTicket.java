package net.rizon.moo.plugin.tickets;


import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.tickets.conf.TicketsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventTicket extends Event
{
	private static final Logger logger = LoggerFactory.getLogger(EventTicket.class);
	
	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			tickets.conf = TicketsConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading tickets configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}
