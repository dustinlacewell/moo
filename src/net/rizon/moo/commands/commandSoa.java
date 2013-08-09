package net.rizon.moo.commands;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.logger;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

class soaCheck extends Thread
{
	private String domain, source, target;
	private boolean debug;

	public soaCheck(final String domain, final String source, final String target, boolean debug)
	{
		this.domain = domain;
		this.source = source;
		this.target = target;
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
					moo.reply(this.source, this.target, this.domain + " has nameserver " + tokens[4]);
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
						moo.reply(this.source, this.target, "Got SOA reply from " + nameserver + " for " + this.domain + ", serial " + tokens[6]);
				}
				
				in.close();
				proc.getOutputStream().close();
				proc.getErrorStream().close();
			}
			
			if (nameservers.size() == 1)
			{
				moo.reply(this.source, this.target, this.domain + " only has one nameserver");
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
					moo.reply(this.source, this.target, "Warning! Nameserver serial numbers are not equal!");
					for (it = nameservers.keySet().iterator(); it.hasNext();)
					{
						nameserver = it.next();
						value = nameservers.get(nameserver);
						
						moo.reply(this.source, this.target, "  " + nameserver + ": " + value);
					}
					return;
				}
			}
			
			moo.reply(this.source, this.target, "All nameserver serial numbers are equal");
		}
		catch (Exception ex)
		{
			logger.getGlobalLogger().log(ex);
		}
	}
}

class commandSoa extends command
{
	public commandSoa(mpackage pkg)
	{
		super(pkg, "!SOA", "Check if SOA records for a domain are valid");
	}
	
	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !SOA host.name");
		moo.notice(source, "Fetches all NS records for host.name and checks if all SOA records");
		moo.notice(source, "associated with those name servers have the same serial number.");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length == 1)
			return;
		else if (params[1].indexOf('.') == -1 || params[1].indexOf(';') != -1 || params[1].indexOf('|') != -1 || params[1].indexOf('&') != -1)
		{
			moo.reply(source, target, "You must give a valid hostname.");
			return;
		}
		
		boolean debug = params.length > 2 && params[2].equalsIgnoreCase("debug");
		
		soaCheck soa = new soaCheck(params[1], source, target, debug);
		soa.start();
	}
}
