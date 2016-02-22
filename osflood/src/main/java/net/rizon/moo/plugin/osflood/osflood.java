package net.rizon.moo.plugin.osflood;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.rizon.moo.Command;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.EventWallops;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.osflood.conf.OsfloodConfiguration;
import org.slf4j.Logger;

public class osflood extends Plugin implements EventListener
{
	private static final Pattern badOSPattern = Pattern.compile("Denied access to OperServ from [^@]+@([^ ]+) .*$");

	@Inject
	private static Logger logger;

	@Inject
	private Protocol protocol;

	@Inject
	private Config config;

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
	}

	@Override
	public void stop()
	{
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
				protocol.akill(host, "+3d", "Services abuse");
				osFlooders.remove(host);

				for (String s : config.flood_channels)
					protocol.privmsg(s, "[FLOOD] Akilled *@" + host + " for flooding OperServ.");
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

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList();
	}

	@Override
	protected void configure()
	{
		bind(osflood.class).toInstance(this);

		bind(OsfloodConfiguration.class).toInstance(conf);

		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);
	}
}
