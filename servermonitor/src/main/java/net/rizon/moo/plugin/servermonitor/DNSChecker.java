package net.rizon.moo.plugin.servermonitor;

import com.google.inject.Inject;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.servermonitor.conf.ServerMonitorConfiguration;
import org.slf4j.Logger;

class DNSChecker extends Thread
{
	@Inject
	private static Logger logger;
	
	@Inject
	private ServerMonitorConfiguration conf;
	
	@Inject
	private Config config;
	
	@Inject
	private Protocol protocol;
	
	@Override
	public void run()
	{
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

		InitialDirContext idir = null;
		try
		{
			idir = new InitialDirContext(env);

			String what = "NS";
			String[] what_array = { what };

			Attributes attributes = idir.getAttributes(conf.domain, what_array);
			Attribute attr = attributes.get(what);

			long serial = -1;

			for (int i = 0; i < attr.size(); ++i)
			{
				final String ns = attr.get(i).toString();

				InitialDirContext idir2 = null;
				try
				{
					env.put(Context.PROVIDER_URL, "dns://" + ns);
					idir2 = new InitialDirContext(env);

					what = "SOA";
					what_array[0] = what;

					Attributes nameserver_attributes = idir2.getAttributes(conf.domain, what_array);
					Attribute nameserver_attr = nameserver_attributes.get(what);

					final String soa = nameserver_attr.get(0).toString();
					final String[] soa_s = soa.split(" ");

					long soa_serial = Long.parseLong(soa_s[2]);

					if (serial == -1)
						serial = soa_serial;
					else if (serial != soa_serial)
						protocol.privmsgAll(config.admin_channels, "DNS: SOA mismatch for " + ns + ": Expected " + serial + " but got " + soa_serial);
				}
				catch (NamingException ex)
				{
					protocol.privmsgAll(config.admin_channels, "DNS: NamingError checking serial for " + ns + ": " + ex);
					logger.warn("Unable to check serial for " + ns, ex);
				}
				catch (Exception ex)
				{
					protocol.privmsgAll(config.admin_channels, "DNS: Error checking serial for " + ns + ": " + ex);
					logger.warn("Unable to check serial for " + ns, ex);
				}
				finally
				{
					try { idir2.close(); } catch (Exception ex) { }
				}
			}
		}
		catch (NamingException ex)
		{
			protocol.privmsgAll(config.admin_channels, "DNS: NamingError checking nameserver serials: " + ex);
			logger.warn("Unable to nameserver serials", ex);
		}
		catch (Exception ex)
		{
			protocol.privmsgAll(config.admin_channels, "DNS: Error checking nameserver serials: " + ex);
			logger.warn("Unable to nameserver serials", ex);
		}
		finally
		{
			try { idir.close(); } catch (Exception ex) { }
		}

		for (String r : conf.check)
		{
			try
			{
				InetAddress.getByName(r);
			}
			catch (UnknownHostException e)
			{
				protocol.privmsgAll(config.admin_channels, "DNS: Unable to resolve " + r + ": " + e.getMessage());
			}
		}
	}
}
