package net.rizon.moo.plugin.proxyscan;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class Connector extends Thread
{
	static final Logger logger = LoggerFactory.getLogger(Connector.class);

	private static final Pattern vars = Pattern.compile("%[^%]+%");

	private final String sourceIp;
	private final Client client;
	
	private proxyscan proxyscan;

	public Connector(String sourceIp, Client client, proxyscan proxyscan)
	{
		this.sourceIp = sourceIp;
		this.client = client;
		this.proxyscan = proxyscan;
	}

	@Override
	public void run()
	{
		Process proc = null;
		String ip = client.getIp();

		try
		{
			String args = proxyscan.getConf().path + " " + proxyscan.getConf().arguments;
			Matcher m = vars.matcher(args);
			while (m.find())
			{
				String var = m.group().substring(1, m.group().length() - 1);

				String replacement;
				if (var.equals("destip"))
					replacement = ip.contains(":") ? "[" + ip + "]" : ip;
				else if (var.equals("bindip"))
					replacement = this.sourceIp.contains(":") ? "[" + this.sourceIp + "]" : this.sourceIp;
				else
					replacement = "";

				args = args.replaceAll(m.group(), replacement);
			}

			ProcessBuilder pb = new ProcessBuilder(args.split(" "));

			for (Iterator<String> it = pb.command().iterator(); it.hasNext();)
				logger.debug("Command part: {}", it.next());

			proc = pb.start();

			String s = null;
			try
			{
				logger.debug("Running process, getting lines...");
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

					proxyscan.akill(ip, Integer.parseInt(port), type, false);
				}
			}
			catch (Exception ex)
			{
				if (s != null)
					logger.info("Exception while processing untrusted proxy scan input", ex);
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
			logger.warn("Exception processing untrusted proxy connection", ex);
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

