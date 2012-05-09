package net.rizon.moo.grapher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import net.rizon.moo.moo;
import net.rizon.moo.timer;

public abstract class graph extends timer
{
	private static File rrd_bin = new File(moo.conf.getRRDBin()),
			rrd_graphdir = new File(moo.conf.getRRDDir());
	private static Runtime rt = Runtime.getRuntime();

	private class dataSource
	{
		String name;
		dataSourceType type;
		long heartbeat;
		long min;
		long max;
	}
	
	private String name;
	private long step;
	private ArrayList<dataSource> dataSources = new ArrayList<dataSource>();
	private roundRobinArchiveType rra_type;
	private long rra_steps;
	private long rra_rows;

	public graph(final String name, long step)
	{
		super(step, true);
		this.name = name + ".rrd";
		this.step = step;
	}
	
	private void exec(final String subcommand, final String args)
	{
		if (rrd_bin.exists() == false || rrd_bin.canExecute() == false)
		{
			System.err.println("RRDTool binary does not exist or is not executable");
			return;
		}
		
		if (rrd_graphdir.exists() == false)
			rrd_graphdir.mkdir();
		
		File database = new File(rrd_graphdir.getAbsolutePath() + File.separatorChar + this.name);
		if (database.getParentFile() != null && database.getParentFile().exists() == false)
			database.getParentFile().mkdir();

		if (database.exists() == false)
		{
			String create_command = "create " + database.getAbsolutePath() + " --step " + this.step;
			
			for (int i = 0; i < this.dataSources.size(); ++i)
			{
				dataSource ds = this.dataSources.get(i);
				String ds_type = null;
				switch (ds.type)
				{
					case DST_GAUGE:
						ds_type = "GAUGE";
						break;
				}
				
				if (ds_type == null)
				{
					System.err.println("Unknown DataSourceType");
					continue;
				}

				create_command += " DS:" + ds.name + ":" + ds_type + ":" + ds.heartbeat + ":" + ds.min + ":" + ds.max;
			}
			
			String rra_type = null;
			switch (this.rra_type)
			{
				case RRA_MAX:
					rra_type = "MAX";
					break;
			}
			
			if (rra_type == null)
			{
				System.err.println("Unknown RoundRobinArchiveType");
				return;
			}

			create_command += " RRA:" + rra_type + ":0.5:" + this.rra_steps + ":" + this.rra_rows;
			
			try
			{
				rt.exec(rrd_bin.getAbsolutePath() + " " + create_command);
			}
			catch (IOException ex)
			{
				System.err.println("Error executing RRDTool with \"" + create_command + "\"");
				ex.printStackTrace();
			}
		}
		
		String command = subcommand + " " + database.getAbsolutePath() + " " + args;
		
		try
		{
			rt.exec(rrd_bin.getAbsolutePath() + " " + command);
		}
		catch (IOException ex)
		{
			System.err.println("Error executing RRDTool with \"" + command + "\"");
			ex.printStackTrace();
		}
	}
	
	protected void addDataSource(final String name, dataSourceType type, long heartbeat, long min, long max)
	{
		dataSource ds = new dataSource();
		ds.name = name;
		ds.type = type;
		ds.heartbeat = heartbeat;
		ds.min = min;
		ds.max = max;
		this.dataSources.add(ds);
	}
	
	protected void setRRA(roundRobinArchiveType rrat, long steps, long rows)
	{
		this.rra_type = rrat;
		this.rra_steps = steps;
		this.rra_rows = rows;
	}
	
	public void update(final String[] values)
	{
		String s = "N";
		for (final String v : values)
			s += ":" + v;
		this.exec("update", s);
	}
	
	public void update(long time, final String[] values)
	{
		String s = String.valueOf(time);
		for (final String v : values)
			s += ":" + v;
		this.exec("update", s);
	}
}