package net.rizon.moo.plugin.osflood;

import com.google.common.eventbus.Subscribe;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventWallops;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.osflood.conf.OsfloodConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class osflood extends Plugin
{
	private static final Logger logger = LoggerFactory.getLogger(osflood.class);
	private final Pattern badOSPattern = Pattern.compile("Denied access to OperServ from [^@]+@([^ ]+) .*$");

	private OsfloodConfiguration conf;

	private final Map<String, OperServFlood> osFlooders = new HashMap<>();
	private long lastexpirycheck = 0;

	public osflood() throws Exception
	{
		super("OSFlood", "Detects and akills users flooding OperServ");
		conf = OsfloodConfiguration.load();
	}

	@Override
	public void start() throws Exception
	{
		Moo.getEventBus().register(this);
	}

	@Override
	public void stop()
	{
		Moo.getEventBus().unregister(this);
	}

	private boolean isExpired(OperServFlood fu)
	{
		return fu.start.before(new Date(System.currentTimeMillis() - conf.time * 60 * 1000));
	}

	@Subscribe
	public void onWallops(EventWallops wallops)
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

		if (!wallops.getSource().startsWith("OperServ!"))
			return;

		Matcher m = badOSPattern.matcher(wallops.getMessage());
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

			if (fu.frequency >= conf.num)
			{
				Moo.akill(host, "+3d", "Services abuse");
				osFlooders.remove(host);

				for (String s : Moo.conf.flood_channels)
					Moo.privmsg(s, "[FLOOD] Akilled *@" + host + " for flooding OperServ.");
			}
		}
	}

	@Subscribe
	public void onReload(OnReload reload)
	{
		try
		{
			conf = OsfloodConfiguration.load();
		}
		catch (Exception ex)
		{
			reload.getSource().reply("Error reloading osflood configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}
}
