package net.rizon.moo.plugin.osflood;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.CommandSource;
import net.rizon.moo.Event;
import net.rizon.moo.Moo;
import net.rizon.moo.plugin.osflood.conf.OsfloodConfiguration;

class EventOSFlood extends Event
{
	private static final Pattern badOSPattern = Pattern.compile("Denied access to OperServ from [^@]+@([^ ]+) .*$");
	protected static final HashMap<String, OperServFlood> osFlooders = new HashMap<String, OperServFlood>();
	private static long lastexpirycheck = 0;

	private boolean isExpired(OperServFlood fu)
	{
		return fu.start.before(new Date(System.currentTimeMillis() - osflood.conf.time * 60 * 1000));
	}

	@Override
	public void onWallops(final String source, final String message)
	{
		// Expire old entries
		if((System.currentTimeMillis() - lastexpirycheck) >= (60*1000))
		{
			for (Iterator<OperServFlood> it = osFlooders.values().iterator(); it.hasNext();)
			{
				if (isExpired(it.next()))
					it.remove();
			}

			lastexpirycheck = System.currentTimeMillis();
		}

		if (!source.startsWith("OperServ!"))
			return;

		Matcher m = badOSPattern.matcher(message);
		if (m.find())
		{
			String host = m.group(1);
			OperServFlood fu = osFlooders.get(host);

			if (fu != null && isExpired(fu))
			{
				osFlooders.remove(host);
				fu = null;
			}

			if (fu == null)
			{
				fu = new OperServFlood(new Date(), 1);
				osFlooders.put(host, fu);
			}
			else
			{
				fu.frequency++;
			}

			if (fu.frequency >= osflood.conf.num)
			{
				Moo.akill(host, "+3d", "Services abuse");
				osFlooders.remove(host);

				for (String s : Moo.conf.flood_channels)
					Moo.privmsg(s, "[FLOOD] Akilled *@" + host + " for flooding OperServ.");
			}
		}
	}

	@Override
	public void onReload(CommandSource source)
	{
		try
		{
			osflood.conf = OsfloodConfiguration.load();
		}
		catch (Exception ex)
		{
			source.reply("Error reloading osflood configuration: " + ex.getMessage());
			osflood.log.log(Level.WARNING, "Unable to reload osflood configuration", ex);
		}
	}
}