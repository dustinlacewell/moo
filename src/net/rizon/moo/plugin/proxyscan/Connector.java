package net.rizon.moo.plugin.proxyscan;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.logging.Level;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

final class ConnectorThread extends Thread
{
	private static final Logger log = Logger.getLogger(ConnectorThread.class.getName());
	private final String proxycheck;
	private final String ip;

	public ConnectorThread(final String proxycheck, final String ip)
	{
		this.proxycheck = proxycheck;
		this.ip = ip;
	}

	@Override
	public void run()
	{
		Process proc = null;

		try
		{
			ProcessBuilder pb = new ProcessBuilder(this.proxycheck, "-c", "chat::" + proxyscan.check_string, "-b",
					Moo.conf.getString("proxyscan.bindip"), "-d", Moo.conf.getString("proxyscan.ip") + ":"
							+ Moo.conf.getString("proxyscan.port"), "-s", "-aaaa", this.ip).redirectErrorStream(true);
			for (Iterator<String> it = pb.command().iterator(); it.hasNext();)
			{
				log.log(Level.FINE, "Command part: " + it.next());
			}
			proc = pb.start();
			
			String s = null;
			try
			{
				log.log(Level.FINE, "Running process, getting lines...");
				InputStreamReader isr = new InputStreamReader(proc.getInputStream());
				BufferedReader in = new BufferedReader(isr);
				if ((s = in.readLine()) != null)
				{
					/*
					 * s now contains the type of proxy, e.g.
					 * "124.28.137.254 wg:23 open"
					 */
					String[] parts_sp = s.split(" ");
					String[] parts_colon = parts_sp[1].split(":");
					String port = parts_colon[1];
					String type = parts_colon[0];
	
					proxyscan.akill(this.ip, Integer.parseInt(port), type, false);
				}
			}
			catch (Exception ex)
			{
				if (s != null)
					log.log(Level.INFO, "Exception while processing untrusted proxy scan input: " + s);
				throw ex;
			}
			finally
			{
				try { proc.getInputStream().close(); } catch (IOException ex) { }
				try { proc.getOutputStream().close(); } catch (IOException ex) { }
				try { proc.getErrorStream().close(); } catch (IOException ex) { }
			}
		}
		catch (Exception ex)
		{
			log.log(Level.INFO, "Exception processing untrusted proxy connection", ex);
		}
		finally
		{
			if (proc != null)
			{
				// This sends a simple SIGTERM, should work well enough. No
				// exceptions to be thrown.
				proc.destroy();
			}
		}
	}
}

public final class Connector
{
	public static final void connect(final String ip)
	{
		String path = Moo.conf.getString("proxyscan.path");
		if (path.isEmpty() == true)
			return;

		File proxycheck = new File(path);
		if (proxycheck.exists() == false || proxycheck.isFile() == false || proxycheck.canExecute() == false)
			return;

		ConnectorThread t = new ConnectorThread(path, ip);
		t.start();
	}
}
