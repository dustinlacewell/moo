package net.rizon.moo.proxyscan;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;

import net.rizon.moo.Logger;

final class ScanListener extends Thread
{
	private static final Logger log = Logger.getLogger(ScanListener.class.getName());
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
			log.log(Level.FINE, "Trying to create listener");
			this.listener = new ServerSocket();
			this.listener.bind(new InetSocketAddress(this.ip, this.port));
			log.log(Level.FINE, "Bound to " + this.ip + ":" + this.port);

			for (Socket client; (client = this.listener.accept()) != null;)
			{
				log.log(Level.FINE, "Accepted client");
				OutputStream s = client.getOutputStream();

				try
				{
					s.write((proxyscan.check_string + "\r\n").getBytes());
					s.flush();
					s.close();
					log.log(Level.FINE, "Wrote check string.");
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
			log.log(ex);
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
			log.log(ex);
		}
	}
}
