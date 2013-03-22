package net.rizon.moo.fun;

import java.util.HashMap;
import java.util.Random;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

class commandRizonTime extends command
{
	private HashMap<String, String> cache = new HashMap<String, String>();
	
	public commandRizonTime(mpackage pkg)
	{
		super(pkg, "!RIZONTIME", "Calculates out a length of time in Rizon Time(tm)");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		Random r = new Random();
		
		String buf = "";
		int time = 1;
		int num = 0;
		
		for (int i = 1; i < params.length; ++i)
		{
			try
			{
				num = Integer.parseInt(params[i]);
				buf += params[i] + " ";
			}
			catch (NumberFormatException ex)
			{
				if (num == 0)
					continue;
				
				buf += params[i] + " ";
				if (params[i].indexOf("year") > -1)
					time += num * 32140800 * (r.nextFloat() + 1.0f) * (r.nextInt(2) + 1);
				else if (params[i].indexOf("month") > -1)
					time += num * 2678400 * (r.nextFloat() + 1.0f) * (r.nextInt(5) + 1);
				else if (params[i].indexOf("week") > -1)
					time += num * 604800 * (r.nextFloat() + 1.0f) * (r.nextInt(7) + 3);
				else if (params[i].indexOf("day") > -1)
					time += num * 86400 * (r.nextFloat() + 1.0f) * (r.nextInt(10) + 5);
				else if (params[i].indexOf("hour") > -1)
					time += num * 3600 * (r.nextFloat() + 1.0f) * (r.nextInt(50) + 24);
				else if (params[i].indexOf("min") > -1)
					time += num * 60 * (r.nextFloat() + 1.0f) * (r.nextInt(100) + 300);
				else
					time += num * (r.nextFloat() + 1.0f) * r.nextInt(200);
			}
		}
		
		buf = buf.trim();
		
		if (this.cache.containsKey(buf.toLowerCase()))
		{
			moo.reply(source, target, buf + " in Rizon Time(tm) is " + this.cache.get(buf.toLowerCase()));
			return;
		}
		
		int years = time / 32140800;
		time %= 32140800;
		
		int months = time / 2678400;
		time %= 2678400;
		
		int days = time / 86400;
		time %= 86400;
		
		int hours = time / 3600;
		time %= 3600;
		
		int minutes = time / 60;
		time %= 60;
		
		int seconds = time;
		
		String out = "";
		if (years > 0)
			out += ", " + years + " year" + (years != 1 ? "s" : "");
		if (months > 0)
			out += ", " + months + " month" + (months != 1 ? "s" : "");
		if (days > 0)
			out += ", " + days + " day" + (days != 1 ? "s" : "");
		if (hours > 0)
			out += ", " + hours + " hour" + (hours != 1 ? "s" : "");
		if (minutes > 0)
			out += ", " + minutes + " minute" + (minutes != 1 ? "s" : "");
		if (years == 0 && seconds > 0)
			out += ", " + seconds + " second" + (seconds != 1 ? "s" : "");
		
		if (out.length() > 2)
		{
			moo.reply(source, target, buf + " in Rizon Time(tm) is " + out.substring(2));
			this.cache.put(buf.toLowerCase(), out.substring(2));
		}
	}
}