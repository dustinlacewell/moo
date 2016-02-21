package net.rizon.moo.plugin.commands;

import com.google.inject.Inject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class soaCheck extends Thread
{
	private CommandSource source;
	private String domain;
	private boolean debug;

	public soaCheck(final String domain, CommandSource source, boolean debug)
	{
		this.domain = domain;
		this.source = source;
		this.debug = debug;
	}

	@Override
	public void run()
	{
		Runtime runtime = Runtime.getRuntime();
		HashMap<String, Integer> nameservers = new HashMap<String, Integer>();

		try
		{
			Process proc = runtime.exec("dig " + this.domain + " NS");
			BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			for (String line; (line = in.readLine()) != null;)
			{
				while (line.indexOf("	") != -1)
					line = line.replaceAll("	", " ");
				while (line.indexOf("  ") != -1)
					line = line.replaceAll("  ", " ");

				if (line.isEmpty() || line.startsWith(this.domain) == false)
					continue;

				String[] tokens = line.split(" ");

				nameservers.put(tokens[4], 0);

				if (this.debug)
					source.reply(this.domain + " has nameserver " + tokens[4]);
			}

			in.close();
			proc.getOutputStream().close();
			proc.getErrorStream().close();

			for (Iterator<String> it = nameservers.keySet().iterator(); it.hasNext();)
			{
				String nameserver = it.next();
				proc = runtime.exec("dig soa " + this.domain + " @" + nameserver);
				in = new BufferedReader(new InputStreamReader(proc.getInputStream()));

				for (String line; (line = in.readLine()) != null;)
				{
					while (line.indexOf("	") != -1)
						line = line.replaceAll("	", " ");
					while (line.indexOf("  ") != -1)
						line = line.replaceAll("  ", " ");

					if (line.isEmpty() || line.startsWith(this.domain) == false || line.indexOf("SOA") == -1)
						continue;

					String[] tokens = line.split(" ");
					nameservers.put(nameserver, Integer.parseInt(tokens[6]));

					if (this.debug)
						source.reply("Got SOA reply from " + nameserver + " for " + this.domain + ", serial " + tokens[6]);
				}

				in.close();
				proc.getOutputStream().close();
				proc.getErrorStream().close();
			}

			if (nameservers.size() == 1)
			{
				source.reply(this.domain + " only has one nameserver");
				return;
			}

			int last = -1;
			for (Iterator<String> it = nameservers.keySet().iterator(); it.hasNext();)
			{
				String nameserver = it.next();
				int value = nameservers.get(nameserver);

				if (last == -1)
					last = value;
				else if (last != value)
				{
					source.reply("Warning! Nameserver serial numbers are not equal!");
					for (it = nameservers.keySet().iterator(); it.hasNext();)
					{
						nameserver = it.next();
						value = nameservers.get(nameserver);

						source.reply("  " + nameserver + ": " + value);
					}
					return;
				}
			}

			source.reply("All nameserver serial numbers are equal");
		}
		catch (Exception ex)
		{
			CommandSoa.logger.warn("Unable to check soa", ex);
		}
	}
}

class CommandSoa extends Command
{
	@Inject
	static Logger logger;

	@Inject
	public CommandSoa(Config conf)
	{
		super("!SOA", "Check if SOA records for a domain are valid");

		this.requiresChannel(conf.staff_channels);
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !SOA host.name");
		source.notice("Fetches all NS records for host.name and checks if all SOA records");
		source.notice("associated with those name servers have the same serial number.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length == 1)
			return;

		if (params[1].indexOf('.') == -1 || params[1].indexOf(';') != -1 || params[1].indexOf('|') != -1 || params[1].indexOf('&') != -1)
		{
			source.reply("You must give a valid hostname.");
			return;
		}

		boolean debug = params.length > 2 && params[2].equalsIgnoreCase("debug");

		soaCheck soa = new soaCheck(params[1], source, debug);
		soa.start();
	}
}
