package net.rizon.moo.plugin.tickets;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.plugin.tickets.conf.TicketsConfiguration;

import java.util.logging.Level;

public class EventTicket extends Event
{
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
			tickets.log.log(Level.WARNING, "Unable to reload tickets configuration", ex);
		}
	}
}
