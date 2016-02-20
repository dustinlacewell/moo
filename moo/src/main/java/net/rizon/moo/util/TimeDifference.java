package net.rizon.moo.util;

import java.util.Date;

public class TimeDifference
{
	public static String difference(Date now, Date then)
	{
		long lnow = now.getTime() / 1000L, lthen = then.getTime() / 1000L;

		long ldiff = now.compareTo(then) > 0 ? lnow - lthen : lthen - lnow;
		int days = 0, hours = 0, minutes = 0;

		if (ldiff == 0)
			return "0 seconds";

		while (ldiff > 86400)
		{
			++days;
			ldiff -= 86400;
		}
		while (ldiff > 3600)
		{
			++hours;
			ldiff -= 3600;
		}
		while (ldiff > 60)
		{
			++minutes;
			ldiff -= 60;
		}

		String buffer = "";
		if (days > 0)
			buffer += days + " day" + (days == 1 ? "" : "s") + " ";
		if (hours > 0)
			buffer += hours + " hour" + (hours == 1 ? "" : "s") + " ";
		if (minutes > 0)
			buffer += minutes + " minute" + (minutes == 1 ? "" : "s") + " ";
		if (ldiff > 0)
			buffer += ldiff + " second" + (ldiff == 1 ? "" : "s") + " ";
		buffer = buffer.trim();

		return buffer;
	}
}
