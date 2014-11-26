package net.rizon.moo.plugin.proxyscan;

import net.rizon.moo.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class ConnectorThread extends Thread
{
	private static final Logger log = Logger.getLogger(ConnectorThread.class.getName());
	private static final Pattern vars = Pattern.compile("%[^%]+%");

	private String source;
	private final String ip;

	public ConnectorThread(String source, final String ip)
	{
		this.source = source;
		this.ip = ip;
	}

	@Override
	public void run()
	{
		Process proc = null;

		try
		{
			String args = proxyscan.conf.path + " " + proxyscan.conf.arguments;
			Matcher m = vars.matcher(args);
			while (m.find())
			{
				String var = m.group().substring(1, m.group().length() - 1);

				String replacement;
				if (var.equals("destip"))
					replacement = this.ip;
				else if (var.equals("bindip"))
					replacement = this.source;
				else
					replacement = "";

				args = args.replaceAll(m.group(), replacement);
			}

			ProcessBuilder pb = new ProcessBuilder(args.split(" "));

			for (Iterator<String> it = pb.command().iterator(); it.hasNext();)
				log.log(Level.FINE, "Command part: " + it.next());

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
	public static final void connect(String source, final String ip)
	{
		String path = proxyscan.conf.path;
		if (path.isEmpty() == true)
			return;

		File proxycheck = new File(path);
		if (proxycheck.exists() == false || proxycheck.isFile() == false || proxycheck.canExecute() == false)
			return;

		ConnectorThread t = new ConnectorThread(source, ip);
		t.start();
	}
}
