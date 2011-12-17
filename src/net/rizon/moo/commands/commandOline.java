package net.rizon.moo.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.server;

final class operComparator implements Comparator<String>
{
	private HashMap<String, Integer> oper_map;

	public operComparator(HashMap<String, Integer> oper_map)
	{
		this.oper_map = oper_map;
	}

	@Override
	public int compare(String arg0, String arg1)
	{
		if (this.oper_map.get(arg0) < this.oper_map.get(arg1))
			return -1;
		else if (this.oper_map.get(arg0) > this.oper_map.get(arg1))
			return 1;
		else
			return 0;
	}
}

public class commandOline extends command
{
	public commandOline()
	{
		super("!OLINE", "View olines");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 1)
			return;
		
		server s = server.findServerAbsolute(source);
		if (s == null)
			s = new server(source);
		if (s.isServices())
			return;
		
		if (params[1].equalsIgnoreCase("COUNT"))
		{			
			int min = 2;
			if (params.length > 2)
			{
				try
				{
					min = Integer.parseInt(params[2]);
				}
				catch (NumberFormatException ex) { }
			}

			HashMap<String, Integer> oper_map = new HashMap<String, Integer>();
			for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
			{
				s = it.next();
				
				for (Iterator<String> it2 = s.olines.iterator(); it2.hasNext();)
				{
					String oline = it2.next();
					
					int old = 0;
					if (oper_map.containsKey(oline))
						old = oper_map.get(oline);
					oper_map.put(oline, old + 1);
				}
			}
			
			int count = 0;
			for (Iterator<String> it = oper_map.keySet().iterator(); it.hasNext();)
			{
				String oper = it.next();
				int oper_count = oper_map.get(oper);
				
				if (oper_count >= min)
					++count;
			}
			
			if (count == 0)
			{
				moo.sock.reply(source, target, "No opers with " + min + " o:lines");
				return;
			}
			
			String opers[] = new String[count];
			int array_count = 0;
			for (Iterator<String> it = oper_map.keySet().iterator(); it.hasNext();)
			{
				String oper = it.next();
				int oper_count = oper_map.get(oper);
				
				if (oper_count >= min)
					opers[array_count++] = oper;
			}
			
			operComparator compare = new operComparator(oper_map);
			Arrays.sort(opers, compare);
			
			moo.sock.reply(source, target, "Oper list with at least " + min + " o:lines");
			for (int i = opers.length; i > 0; --i)
			{
				String oper = opers[i - 1];
				int oper_count = oper_map.get(oper);

				moo.sock.reply(source, target, oper + ": " + oper_count);
			}
			
			return;
		}

		boolean found = false;
		s = server.findServer(params[1]);
		if (s != null)
		{
			String buffer = "o:lines for " + s.getName() + ": ";
			if (s.olines.isEmpty())
				buffer += "none";
			else
			{
				for (Iterator<String> it = s.olines.iterator(); it.hasNext();)
				{
					String oline = it.next();
					buffer += oline + ", ";
				}
				buffer = buffer.substring(0, buffer.length() - 2);
			}
			
			moo.sock.reply(source, target, buffer);
			found = true;
		}
		
		String servers = "";
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			s = it.next();
			
			if (s.olines.contains(params[1]))
				servers += s.getName() +  ", ";
		}
		if (!servers.isEmpty())
		{
			servers = servers.substring(0, servers.length() - 2);
			moo.sock.reply(source, target, params[1] + " has o:lines on: " + servers);
			found = true;
		}
		if (found == false)
			moo.sock.reply(source, target, "No o:lines for " + params[1]);
	}
}
