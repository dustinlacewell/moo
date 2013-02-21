package net.rizon.moo.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.event;
import net.rizon.moo.message;
import net.rizon.moo.server;

/* end of stats */
public class message219 extends message
{
	public message219()
	{
		super("219");
	}
	
	private static String generateFlagDiff(final String oldflags, final String newflags)
	{
		String del = null, add = null;
		
		for (char c : oldflags.toCharArray())
		{
			if (newflags.indexOf(c) == -1)
			{
				if (del == null)
					del = "-" + c;
				else
					del += c;
			}
		}
		if (del == null)
			del = "";

		for (char c : newflags.toCharArray())
		{
			if (oldflags.indexOf(c) == -1)
			{
				if (add == null)
					add = "+" + c;
				else
					add += c;
			}
		}
		if (add == null)
			add = "";
		
		return add + del;
	}
	
	@Override
	public void run(String source, String[] message)
	{
		server serv = server.findServerAbsolute(source);
		if (serv == null)
			serv = new server(source);
		
		if (message[1].equals("c"))
		{
			if (serv.clines.isEmpty() == false)
			{
				for (Iterator<String> it = serv.clines.iterator(); it.hasNext();)
				{
					String s = it.next();
					
					if (serv.clines_work.contains(s) == false)
					{
						for (event e : event.getEvents())
							e.OnXLineDel(serv, 'C', s);
					}
				}
				
				for (Iterator<String> it = serv.clines_work.iterator(); it.hasNext();)
				{
					String s = it.next();
					
					if (serv.clines.contains(s) == false)
					{
						for (event e : event.getEvents())
							e.OnXLineAdd(serv, 'C', s);
					}
				}
			}
			
			serv.clines = serv.clines_work;
			serv.clines_work = new HashSet<String>();
		}
		else if (message[1].equals("o"))
		{
			if (serv.olines.isEmpty() == false)
			{
				for (Iterator<String> it = serv.olines.keySet().iterator(); it.hasNext();)
				{
					String s = it.next();
					
					if (serv.olines_work.keySet().contains(s) == false)
					{
						for (event e : event.getEvents())
						{
							e.OnXLineDel(serv, 'O', s);
						}
					}
					else
					{
						String oldflags = serv.olines.get(s);
						String newflags = serv.olines_work.get(s);
						if (oldflags != null && !newflags.equals(oldflags))
						{
							for (event e : event.getEvents())
								e.OnOLineChange(serv, s, generateFlagDiff(oldflags, newflags));
						}
					}
				}
				
				for (Iterator<String> it = serv.olines_work.keySet().iterator(); it.hasNext();)
				{
					String s = it.next();
					
					if (serv.olines.keySet().contains(s) == false)
					{
						for (event e : event.getEvents())
						{
							e.OnXLineAdd(serv, 'O', s);
						}
					}
				}
			}
			
			serv.olines = serv.olines_work;
			serv.olines_work = new HashMap<String, String>();
		}
	}
}
