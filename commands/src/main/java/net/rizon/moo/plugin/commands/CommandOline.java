package net.rizon.moo.plugin.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

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

final class serverComparator implements Comparator<Server>
{
	@Override
	public int compare(Server arg0, Server arg1)
	{
		if (arg0.olines.size() < arg1.olines.size())
			return -1;
		else if (arg0.olines.size() > arg1.olines.size())
			return 1;
		else
			return 0;
	}
}

class CommandOline extends Command
{
	private static final serverComparator servComparator = new serverComparator();

	public CommandOline(Plugin pkg)
	{
		super(pkg, "!OLINE", "View olines");

		this.requiresChannel(Moo.conf.staff_channels);
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !OLINE { COUNT [num] | SERVER [[{<,<=,>,>=,=}]num] | <oper> | <server> }");
		source.notice("If COUNT is given, all known O:lines will be searched and opers with at least");
		source.notice("two (2) O:lines will be returned. If a number is given as well, the minimum");
		source.notice("number of O:lines can be changed.");
		source.notice("If SERVER is given, all servers will be searched for O:lines and servers with at");
		source.notice("least two (2) O:lines will be returned by default. If a number is given as well, the minimum");
		source.notice("number of O:lines can be changed. If one of <, <=, >, >=, = is supplied, only servers with");
		source.notice("less than, less than or equal to, greater than, greater than or equal to, equal to the");
		source.notice("number supplied, respectively, will be returned.");
		source.notice("If an oper name is given, the servers (s)he has an O:line on will be shown.");
		source.notice("If a server name is given, the O:lines on that server will be shown.");
	}

	enum Type
	{
		lt,
		le,
		gt,
		ge,
		e
	}

	@Override
	public void execute(CommandSource source, String[] params)
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
			for (Server s : Server.getServers())
			{
				if (s.isServices() || s.olines == null)
					continue;

				for (Iterator<String> it2 = s.olines.keySet().iterator(); it2.hasNext();)
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
				source.reply("No opers with " + min + " o:lines");
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

			source.reply("Oper list with at least " + min + " o:lines");
			for (int i = opers.length; i > 0; --i)
			{
				String oper = opers[i - 1];
				int oper_count = oper_map.get(oper);

				source.reply(oper + ": " + oper_count);
			}
		}
		else if (params[1].equalsIgnoreCase("SERVER"))
		{
			int count = 2;
			Type t = Type.ge;
			if (params.length > 2)
			{
				int offset = 0;
				if (params[2].startsWith("<="))
				{
					t = Type.le;
					offset = 2;
				}
				else if (params[2].startsWith("<"))
				{
					t = Type.lt;
					offset = 1;
				}
				else if (params[2].startsWith(">="))
				{
					t = Type.ge;
					offset = 2;
				}
				else if (params[2].startsWith(">"))
				{
					t = Type.gt;
					offset = 1;
				}
				else if (params[2].startsWith("="))
				{
					t = Type.e;
					offset = 1;
				}
				else
				{
					t = Type.gt;
					offset = 0;
				}

				try
				{
					count = Integer.parseInt(params[2].substring(offset));
				}
				catch (NumberFormatException ex) { }
			}

			Server servers[] = Server.getServers();
			Arrays.sort(servers, servComparator);

			source.reply("Servers with " + (params.length > 2 ? (Character.isDigit(params[2].charAt(0)) ? ">=" + count : params[2]) : ">=" + count) + " o:lines:");

			for (int i = servers.length; i > 0; --i)
			{
				Server s = servers[i - 1];

				if (s.isServices())
					continue;

				switch (t)
				{
					case e:
						if (s.olines.size() != count)
							continue;
						break;
					case lt:
						if (!(s.olines.size() < count))
							continue;
						break;
					case le:
						if (!(s.olines.size() <= count))
							continue;
						break;
					case gt:
						if (!(s.olines.size() > count))
							continue;
						break;
					case ge:
						if (!(s.olines.size() >= count))
							continue;
						break;
				}

				String olines = s.olines.toString();
				olines = olines.substring(1, olines.length() - 1);

				source.reply(s.getName() + ": " + olines);
			}
		}
		else
		{
			boolean found = false;
			Server s = Server.findServer(params[1]);
			if (s != null)
			{
				String buffer = "o:lines for " + s.getName() + ": ";
				if (s.olines.isEmpty())
					buffer += "none";
				else
				{
					for (Iterator<String> it = s.olines.keySet().iterator(); it.hasNext();)
					{
						String oline = it.next();
						buffer += oline + ", ";
					}
					buffer = buffer.substring(0, buffer.length() - 2);
				}

				source.reply(buffer);
				found = true;
			}

			String servers = "";
			for (Server s2 : Server.getServers())
			{
				if (s2.isServices())
					continue;

				if (s2.olines.keySet().contains(params[1]))
					servers += s2.getName() +  ", ";
			}
			if (!servers.isEmpty())
			{
				servers = servers.substring(0, servers.length() - 2);
				source.reply(params[1] + " has o:lines on: " + servers);
				found = true;
			}
			if (found == false)
				source.reply("No o:lines for " + params[1]);
		}
	}
}
