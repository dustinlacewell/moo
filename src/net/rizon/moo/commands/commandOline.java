package net.rizon.moo.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
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

final class serverComparator implements Comparator<server>
{
	@Override
	public int compare(server arg0, server arg1)
	{
		if (arg0.olines.size() < arg1.olines.size())
			return -1;
		else if (arg0.olines.size() > arg1.olines.size())
			return 1;
		else
			return 0;
	}
}

public class commandOline extends command
{
	private static final serverComparator servComparator = new serverComparator();

	public commandOline(mpackage pkg)
	{
		super(pkg, "!OLINE", "View olines");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length <= 1)
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
			for (server s : server.getServers())
			{	
				if (s.isServices())
					continue;
				
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
		}
		else if (params[1].equalsIgnoreCase("SERVER"))
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

			server servers[] = server.getServers();
			Arrays.sort(servers, servComparator);
			
			moo.sock.reply(source, target, "Servers with a least " + min +  " o:lines:");
			
			for (int i = servers.length; i > 0; --i)
			{
				server s = servers[i - 1];

				if (s.isServices() || s.olines.size() < min)
					continue;
				
				String olines = s.olines.toString();
				olines = olines.substring(1, olines.length() - 1);
				
				moo.sock.reply(source, target, s.getName() + ": " + olines); 
			}
		}
		else
		{
			boolean found = false;
			server s = server.findServer(params[1]);
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
			for (server s2 : server.getServers())
			{
				if (s2.isServices())
					continue;
				
				if (s2.olines.contains(params[1]))
					servers += s2.getName() +  ", ";
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
}
