package net.rizon.moo.plugin.grapher;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import net.rizon.moo.plugin.grapher.conf.GrapherConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Graph implements Runnable
{
	private static final Logger logger = LoggerFactory.getLogger(Graph.class);

	private static File rrd_bin,
			rrd_graphdir;
	private static Runtime rt = Runtime.getRuntime();

	private class dataSource
	{
		String name;
		DataSourceType type;
		long heartbeat;
		long min;
		long max;
	}

	private String name;
	private long step;
	private ArrayList<dataSource> dataSources = new ArrayList<dataSource>();
	private RoundRobinArchiveType rra_type;
	private long rra_steps;
	private long rra_rows;

	public Graph(GrapherConfiguration conf, String name)
	{
		this.name = name + ".rrd";
		this.step = step;

		rrd_bin = new File(conf.bin);
		rrd_graphdir = new File(conf.dir);
	}

	private void exec(final String subcommand, final String args)
	{
		if (rrd_bin.exists() == false || rrd_bin.canExecute() == false)
		{
			logger.warn("RRDTool binary does not exist or is not executable");
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
					logger.warn("Unknown DataSourceType");
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
				logger.warn("Unknown RoundRobinArchiveType");
				return;
			}

			create_command += " RRA:" + rra_type + ":0.5:" + this.rra_steps + ":" + this.rra_rows;

			try
			{
				Process p = rt.exec(rrd_bin.getAbsolutePath() + " " + create_command);
				p.getInputStream().close();
				p.getOutputStream().close();
				p.getErrorStream().close();
			}
			catch (IOException ex)
			{
				logger.warn("Error executing RRDTool with \"" + create_command + "\"", ex);
			}
		}

		String command = subcommand + " " + database.getAbsolutePath() + " " + args;

		try
		{
			Process p = rt.exec(rrd_bin.getAbsolutePath() + " " + command);
			p.getInputStream().close();
			p.getOutputStream().close();
			p.getErrorStream().close();
		}
		catch (IOException ex)
		{
			logger.warn("Error executing RRDTool with \"" + command + "\"", ex);
		}
	}

	protected void addDataSource(final String name, DataSourceType type, long heartbeat, long min, long max)
	{
		dataSource ds = new dataSource();
		ds.name = name;
		ds.type = type;
		ds.heartbeat = heartbeat;
		ds.min = min;
		ds.max = max;
		this.dataSources.add(ds);
	}

	protected void setRRA(RoundRobinArchiveType rrat, long steps, long rows)
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