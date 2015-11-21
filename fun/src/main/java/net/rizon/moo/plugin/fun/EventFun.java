package net.rizon.moo.plugin.fun;

import com.google.common.eventbus.Subscribe;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Moo;
import net.rizon.moo.events.EventPrivmsg;
import net.rizon.moo.events.EventQuit;

class TimedKill implements Runnable
{
	private String dest, reason;

	public TimedKill(final String dest, final String reason)
	{
		this.dest = dest;
		this.reason = reason;
	}

	@Override
	public void run()
	{
		Moo.kill(this.dest, this.reason);
	}
}

class EventFun
{
	private static final Pattern killPattern = Pattern.compile("Killed \\(([^ ]*) \\(([^)]*)\\)\\)");

	private static HashMap<String, Integer> birthdays = new HashMap<String, Integer>(),
			welcome = new HashMap<String, Integer>(),
			gratz = new HashMap<String, Integer>();

	@Subscribe
	public void onPrivmsg(EventPrivmsg evt)
	{
		String message = evt.getMessage(), source = evt.getSource(), channel = evt.getChannel();
		
		if (message.startsWith("\1ACTION pets " + Moo.me.getNick()))
			Moo.privmsg(channel, "\1ACTION moos\1");
		else if (message.startsWith("\1ACTION milks " + Moo.me.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			Moo.privmsg(channel, "\1ACTION kicks " + nick + " in the face\1");
		}
		else if (message.startsWith("\1ACTION feeds " + Moo.me.getNick()))
			Moo.privmsg(channel, "\1ACTION eats happily\1");
		else if (message.startsWith("\1ACTION kicks " + Moo.me.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			Moo.privmsg(channel, "\1ACTION body slams " + nick + "\1");
		}
		else if (message.startsWith("\1ACTION brands " + Moo.me.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			boolean kill = new Random().nextInt(100) == 0;

			if (kill == false)
				Moo.privmsg(channel, "\1ACTION headbutts " + nick + " and proceeds to stomp on their lifeless body\1");
			else
			{
				Moo.privmsg(channel, "FEEL THE WRATH OF " + Moo.me.getNick().toUpperCase());
				Moo.kill(nick, "HOW DARE YOU ATTEMPT TO BRAND " + Moo.me.getNick().toUpperCase());
			}
		}
		else if (message.startsWith("\1ACTION tips " + Moo.me.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			Moo.privmsg(channel, "\1ACTION inadvertently falls on " + nick + " and crushes them\1");
		}
		else if (message.startsWith("\1ACTION slaughters " + Moo.me.getNick()))
		{
			int e = source.indexOf('!');
			String nick = source.substring(0, e != -1 ? e : source.length());
			Moo.privmsg(channel, "\1ACTION runs " + nick + " through a food processor and proceeds to eat them\1");
		}
	}

	@Subscribe
	public void onQuit(EventQuit evt)
	{
		String reason = evt.getReason(), source = evt.getSource();
		
		Matcher m = killPattern.matcher(reason);
		if (m.find())
		{
			final String killee = source.substring(0, source.indexOf('!')),
					kill_reason = m.group(2);

			if (kill_reason.toLowerCase().indexOf("birthday") > -1)
			{
				int i = birthdays.containsKey(killee.toLowerCase()) ? birthdays.get(killee.toLowerCase()) : 0;
				birthdays.put(killee.toLowerCase(), i + 1);

				if (i == 3)
				{
					String my_kill_reason = "happy birthday!!";
					Random r = new Random();
					for (i = 0; i < r.nextInt(30) + 10; ++i)
						my_kill_reason += (char) (33 + r.nextInt(14));
					if (r.nextBoolean())
						my_kill_reason = my_kill_reason.toUpperCase();

					Moo.schedule(new TimedKill(killee, my_kill_reason), new Random().nextInt(250) + 60, TimeUnit.SECONDS);
				}
			}
			else if (kill_reason.toLowerCase().indexOf("welcome") > -1)
			{
				int i = welcome.containsKey(killee.toLowerCase()) ? welcome.get(killee.toLowerCase()) : 0;
				welcome.put(killee.toLowerCase(), i + 1);

				if (i == 3)
				{
					String my_kill_reason = "welcome to ";
					Random r = new Random();
					if (r.nextInt(10) == 0)
						my_kill_reason += "HELL";
					else
						my_kill_reason += "rizon!!";
					for (i = 0; i < r.nextInt(30) + 10; ++i)
						my_kill_reason += (char) (33 + r.nextInt(14));
					if (r.nextBoolean())
						my_kill_reason = my_kill_reason.toUpperCase();

					Moo.schedule(new TimedKill(killee, my_kill_reason), new Random().nextInt(250) + 60, TimeUnit.SECONDS);
				}
			}
			else if (kill_reason.toLowerCase().indexOf("congrat") > -1 || kill_reason.toLowerCase().indexOf("gratz") > -1)
			{
				int i = gratz.containsKey(killee.toLowerCase()) ? gratz.get(killee.toLowerCase()) : 0;
				gratz.put(killee.toLowerCase(), i + 1);

				if (i == 3)
				{
					String my_kill_reason = "congratulations!!";
					Random r = new Random();
					if (r.nextBoolean())
						my_kill_reason = my_kill_reason.toUpperCase();

					Moo.schedule(new TimedKill(killee, my_kill_reason), new Random().nextInt(250) + 60, TimeUnit.SECONDS);
				}
			}
		}
	}
}