package net.rizon.moo.commands;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class message227 extends message
{
	public message227()
	{
		super("227");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 4)
			return;
		
		final String name = message[2];
		
		try
		{
			int count = Integer.parseInt(message[3]);
			
			long i;
			if (commandDnsbl.dnsbl_values.containsKey(name))
			{
				i = commandDnsbl.dnsbl_values.get(name);
				i += count;
			}
			else
				i = count;
			commandDnsbl.dnsbl_values.put(name, i);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}

class dnsblComparator implements Comparator<String>
{
	public static dnsblComparator cmp = new dnsblComparator();

	@Override
	public int compare(String arg0, String arg1)
	{
		long val0 = commandDnsbl.dnsbl_values.get(arg0), val1 = commandDnsbl.dnsbl_values.get(arg1);
		if (val0 < val1)
			return -1;
		else if (val0 > val1)
			return 1;
		else
			return 0;
	}
}

class message219_dnsbl extends message
{
	public message219_dnsbl()
	{
		super("219");
	}
	
	@Override
	public void run(String source, String[] message)
	{
		if (message[1].equals("B") == false)
			return;
		
		commandDnsbl.waiting_on.remove(source);
		
		if (commandDnsbl.waiting_on.isEmpty() && commandDnsbl.target_chan != null && commandDnsbl.target_source != null)
		{
			moo.sock.reply(commandDnsbl.target_source, commandDnsbl.target_chan, "DNSBL counts:");
			
			long total = 0;
			for (Iterator<String> it = commandDnsbl.dnsbl_values.keySet().iterator(); it.hasNext();)
				total += commandDnsbl.dnsbl_values.get(it.next());
			
			String keys_sorted[] = new String[commandDnsbl.dnsbl_values.size()];
			commandDnsbl.dnsbl_values.keySet().toArray(keys_sorted);
			Arrays.sort(keys_sorted, dnsblComparator.cmp);
			
			for (int i = keys_sorted.length; i > 0; --i)
			{
				String name = keys_sorted[i - 1];
				long value = commandDnsbl.dnsbl_values.get(name);
				float percent = total > 0 ? ((float) value / (float) total * (float) 100) : 0;
				int percent_i = Math.round(percent);
				
				moo.sock.reply(commandDnsbl.target_source, commandDnsbl.target_chan, name + ": " + value + " (" + percent_i + "%)");
			}
			
			commandDnsbl.dnsbl_values.clear();
			commandDnsbl.target_chan = commandDnsbl.target_source = null;
		}
	}
}

public class commandDnsbl extends command
{
	@SuppressWarnings("unused")
	private static message227 msg_227 = new message227();
	@SuppressWarnings("unused")
	private static message219_dnsbl msg_219 = new message219_dnsbl();
	
	public static HashSet<String> waiting_on = new HashSet<String>();
	public static HashMap<String, Long> dnsbl_values = new HashMap<String, Long>();
	public static String target_chan, target_source;
	
	public commandDnsbl(mpackage pkg)
	{
		super(pkg, "!DNSBL", "Views DNSBL counts");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		waiting_on.clear();
		dnsbl_values.clear();
		target_chan = target;
		target_source = source;

		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();
			if (s.getSplit() == null && !s.isServices())
			{
				moo.sock.write("STATS B " + s.getName());
				waiting_on.add(s.getName());
			}
		}
	}
}
