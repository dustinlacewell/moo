package net.rizon.moo.plugin.tickets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Date;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

class TicketChecker extends Thread
{
	private static final int reminder = 30; // minutes
	private static final Pattern pattern = Pattern.compile("<tr><td><a href=\"/akills/view/([0-9]*)\">#\\1</a></td><td>(.*?)</td><td>(.*?)</td><td>(.*?)</td><td class=\"date\">(.*?)</td><td(?:.*?)>(?:At )?(.*?)</td><td>(.*?)</td></tr>");
	private static boolean firstRun = true;

	@Override
	public void run()
	{
		HttpURLConnection connection = null;

		try
		{
			URL url = new URL(tickets.conf.url);
			connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod("POST");
			connection.setConnectTimeout(15 * 1000);
			connection.setReadTimeout(15 * 1000);
			connection.setDoOutput(true);

			String postData = "username=" + URLEncoder.encode(tickets.conf.username, "UTF-8") + "&password=" + URLEncoder.encode(tickets.conf.password, "UTF-8");

			OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream());
			writer.write(postData);
			writer.flush();

			BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

			Date now = new Date();
			int reminded = 0;

			for (String line; (line = reader.readLine()) != null;)
			{
				Matcher m = pattern.matcher(line);
				while (m.find())
				{
					String id = m.group(1), type = m.group(2), ip = m.group(3), contact = m.group(4),
							date = m.group(5), state = m.group(6), lastReplier = m.group(7);

					int ticket;
					try
					{
						ticket = Integer.parseInt(id);
					}
					catch (NumberFormatException ex)
					{
						continue;
					}

					String message = null;
					Ticket t = tickets.tickets.get(ticket);

					if (t == null)
					{
						message = "new ticket";
					}
					else if (!t.lastReplier.equalsIgnoreCase(lastReplier))
					{
						message = "new reply from " + lastReplier;
						t.lastReplier = lastReplier;
					}
					else if (t.nextReminder.before(now))
					{
						if (!state.equals("Pending"))
							continue;

						if (reminded++ > 0)
							continue;

						t.nextReminder = new Date(now.getTime() + ((reminder * ++t.reminded) * 60 * 1000));
						message = "reminder";
					}

					if (message == null)
						continue;

					message = "#" + id + ": " + message + " (" + date + ") :: " + type + " :: " + ip + " :: " + contact + " :: http://abuse.rizon.net/" + id;

					if (!firstRun)
						Moo.privmsgAll(Moo.conf.kline_channels, message);

					if (t == null)
					{
						t = new Ticket();
						t.lastReplier = lastReplier;
						t.nextReminder = new Date(now.getTime() + (reminder * 60 * 1000));
						tickets.tickets.put(ticket, t);
					}
				}
			}

			if (!firstRun)
				if (reminded > 1)
					Moo.privmsgAll(Moo.conf.kline_channels, "Remaining tickets: " + (reminded - 1));

			firstRun = false;
		}
		catch (Exception ex)
		{
			Logger.getGlobalLogger().log(Level.WARNING, "Unable to check tickets", ex);
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
