package net.rizon.moo.plugin.tickets;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.List;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.tickets.conf.TicketsConfiguration;
import org.slf4j.Logger;

class TicketChecker extends Thread
{
	@Inject
	private static Logger logger;
	
	@Inject
	private TicketsConfiguration conf;
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private Config config;
	
	private static final int reminder = 30; // minutes
	private static boolean firstRun = true;

	@Override
	public void run()
	{
		HttpURLConnection connection = null;

		try
		{
			URL url = new URL(conf.url + "?referrer=json"); // Automatically redirects us to the json endpoint
			connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setConnectTimeout(15 * 1000);
			connection.setReadTimeout(15 * 1000);
			connection.setDoOutput(true);

			String postData = "username=" + URLEncoder.encode(conf.username, "UTF-8") + "&password=" + URLEncoder.encode(conf.password, "UTF-8");

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(postData);
			writer.flush();

			Date now = new Date();
			int reminded = 0;

			List<WebTicket> ticks = new Gson().fromJson(
					new InputStreamReader(connection.getInputStream(), "UTF-8"),
					new TypeToken<List<WebTicket>>(){}.getType()
			);

			for (WebTicket wt : ticks)
			{
				int ticket = wt.getTicket();
				TicketState ticketState = TicketState.create(wt.getStatus());

				String message = null;
				Ticket t = tickets.tickets.get(ticket);

				if (t == null)
				{
					message = "new ticket";
				}
				else if (ticketState == TicketState.RESOLVED && t.getState() != TicketState.RESOLVED)
				{
					// Notify overlords that ticket has been resolved.
					message = "resolved by " + wt.getLastReplier();
				}
				else if (ticketState == TicketState.CLOSED && t.getState() != TicketState.CLOSED)
				{
					// Notify overlords that ticket has been closed.
					message = "closed by " + wt.getLastReplier();
				}
				else if (ticketState != TicketState.CLOSED && t.getState() == TicketState.CLOSED)
				{
					// Notify overlords that ticket has been reopened.
					message = "reopened by " + wt.getLastReplier();
				}
				else if ((ticketState == TicketState.PENDING || ticketState == TicketState.IN_PROGRESS) && !t.getLastReplier().equalsIgnoreCase(wt.getLastReplier()))
				{
					message = "new reply from " + wt.getLastReplier();
					t.setLastReplier(wt.getLastReplier());
				}
				else if (ticketState == TicketState.PENDING && t.getNextReminder().before(now))
				{
					if (reminded++ > 0)
						continue;

					int r = t.getReminded();
					t.setReminded(++r);

					t.setNextReminder(new Date(now.getTime() + ((reminder * r) * 60 * 1000)));
					message = "reminder";
				}

				if (message == null)
				{
					continue;
				}

				String type = wt.getHostNumber() > 0 ? "SLI" : "Ban";

				message = "#" + ticket + ": " + message + " (" + wt.getAdded() + ") :: " + type + " :: " + wt.getIp() + " :: " + wt.getContactName() + " :: https://abuse.rizon.net/" + ticket;

				if (!firstRun)
					protocol.privmsgAll(config.kline_channels, message);

				if (t == null)
				{
					t = new Ticket();
					t.setLastReplier(wt.getLastReplier());
					t.setNextReminder(new Date(now.getTime() + (reminder * 60 * 1000)));
					t.setState(ticketState);
					tickets.tickets.put(ticket, t);
				}
				else
				{
					t.setState(ticketState);
				}
			}

			if (!firstRun)
				if (reminded > 1)
					protocol.privmsgAll(config.kline_channels, "Remaining tickets: " + (reminded - 1));

			firstRun = false;
		}
		catch (Exception ex)
		{
			logger.warn("Unable to check tickets", ex);
		}
		finally
		{
			try { connection.getInputStream().close(); }
			catch (Exception ex) { }

			try { connection.getOutputStream().close(); }
			catch (Exception ex) { }

			try { connection.getErrorStream().close(); }
			catch (Exception ex) { }
		}
	}
}
