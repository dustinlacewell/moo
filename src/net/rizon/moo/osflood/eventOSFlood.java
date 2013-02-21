package net.rizon.moo.osflood;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.event;
import net.rizon.moo.moo;

class eventOSFlood extends event
{
	private static final Pattern badOSPattern = Pattern.compile("Denied access to OperServ from [^@]+@([^ ]+) .*$");
	protected static final HashMap<String, operServFlood> osFlooders = new HashMap<String, operServFlood>();
	private static long lastexpirycheck = 0;
	
	private boolean isExpired(operServFlood fu)
	{
		return fu.start.before(new Date(System.currentTimeMillis() - moo.conf.getOSFloodTime()*60*1000));
	}
	
	@Override
	public void onWallops(final String source, final String message)
	{
		// Expire old entries
		if((System.currentTimeMillis() - lastexpirycheck) >= (60*1000))
		{
			for (Iterator<operServFlood> it = osFlooders.values().iterator(); it.hasNext();)
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
			operServFlood fu = osFlooders.get(host);
			
			if (fu != null && isExpired(fu))
			{
				osFlooders.remove(host);
				fu = null;
			}
			
			if (fu == null)
			{
				fu = new operServFlood(new Date(), 1);
				osFlooders.put(host, fu);
			}
			else
			{
				fu.frequency++;
			}
			
			if (fu.frequency >= moo.conf.getOSFloodNum())
			{
				moo.akill(host, "+3d", "Services abuse");
				osFlooders.remove(host);
				
				for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
					moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD] Akilled *@" + host + " for flooding OperServ.");
			}
		}
	}
}