package net.rizon.moo.plugin.proxyscan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

final class ScanListener extends Thread
{
	private static final Logger logger = LoggerFactory.getLogger(ScanListener.class);
	
	private static final Pattern typeMatchPatter = Pattern.compile("[a-z0-9_]+");

	private final String ip;
	private final int port;
	private ServerSocket listener;

	public ScanListener(final String ip, final int port)
	{
		this.ip = ip;
		this.port = port;
	}

	@Override
	public void run()
	{
		try
		{
			logger.debug("Trying to create listener");
			this.listener = new ServerSocket();
			this.listener.bind(new InetSocketAddress(this.ip, this.port));
			logger.debug("Bound to {}:{}", this.ip, this.port);

			for (Socket client; (client = this.listener.accept()) != null;)
			{
				logger.debug("Accepted client");
				InputStream is = client.getInputStream();
				OutputStream s = client.getOutputStream();

				try
				{
					s.write((proxyscan.conf.check_string + "\r\n").getBytes());
					s.flush();
					logger.debug("Wrote check string.");

					client.setSoTimeout(10 * 1000);
					byte[] b = new byte[64];
					is.read(b);

					/* protocol_name:ip:port\n */
					String[] str = new String(b).trim().split(":");
					if (str.length == 3)
					{
						try
						{
							int p = Integer.parseInt(str[2]); /* port */
							String type = str[0];

							Matcher m = typeMatchPatter.matcher(type);
							if (m.matches())
							{
								if (p > 0 && p < 65535)
								{
									InetSocketAddress them = (InetSocketAddress) client.getRemoteSocketAddress();
									proxyscan.akill(them.getAddress().getHostAddress(), p, type, true);
								}
							}
						}
						catch (NumberFormatException ex)
						{
							logger.debug("Non numeric port for proxy: {}", str[2]);
						}
					}
				}
				catch (IOException ex)
				{
					// I guess this can happen somehow?
				}
				finally
				{
					try { s.close(); } catch (IOException ex) {}
				}
			}
		}
		catch (Exception ex)
		{
			logger.warn("Error running scan listener", ex);
		}
	}

	protected void shutdown()
	{
		try
		{
			this.listener.close();
		}
		catch (Exception ex)
		{
			logger.error("Unable to shutdown listener", ex);
		}
	}
}
