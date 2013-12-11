package net.rizon.moo.protocol;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Server;

/* end of stats */
public class Message219 extends Message
{
	public Message219()
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
		Server serv = Server.findServerAbsolute(source);
		if (serv == null)
			serv = new Server(source);
		
		if (message[1].equals("c"))
		{
			if (serv.clines.isEmpty() == false)
			{
				for (Iterator<String> it = serv.clines.iterator(); it.hasNext();)
				{
					String s = it.next();
					
					if (serv.clines_work.contains(s) == false)
					{
						for (Event e : Event.getEvents())
							e.OnXLineDel(serv, 'C', s);
					}
				}
				
				for (Iterator<String> it = serv.clines_work.iterator(); it.hasNext();)
				{
					String s = it.next();
					
					if (serv.clines.contains(s) == false)
					{
						for (Event e : Event.getEvents())
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
						for (Event e : Event.getEvents())
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
							for (Event e : Event.getEvents())
								e.OnOLineChange(serv, s, generateFlagDiff(oldflags, newflags));
						}
					}
				}
				
				for (Iterator<String> it = serv.olines_work.keySet().iterator(); it.hasNext();)
				{
					String s = it.next();
					
					if (serv.olines.keySet().contains(s) == false)
					{
						for (Event e : Event.getEvents())
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