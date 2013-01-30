package net.rizon.moo.servermonitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import net.rizon.moo.moo;

class dnsChecker extends Thread
{
	private static final String[] regions = { "na", "eu", "us", "ca", "nl", "fr", "de" };
	private static final String domain = "rizon.net";
	
	@Override
	public void run()
	{
		Properties env = new Properties();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");

		try
		{
			InitialDirContext idir = new InitialDirContext(env);
			
			String what = "NS";
			String[] what_array = { what };
			
			Attributes attributes = idir.getAttributes(domain, what_array);
			Attribute attr = attributes.get(what);
			
			long serial = -1;
			
			for (int i = 0; i < attr.size(); ++i)
			{
				final String ns = attr.get(i).toString();
				
				try
				{
					env.put(Context.PROVIDER_URL, "dns://" + ns);
					idir = new InitialDirContext(env);
					
					what = "SOA";
					what_array[0] = what;
					
					Attributes nameserver_attributes = idir.getAttributes(domain, what_array);
					Attribute nameserver_attr = nameserver_attributes.get(what);
					
					final String soa = nameserver_attr.get(0).toString();
					final String[] soa_s = soa.split(" ");
					
					long soa_serial = Long.parseLong(soa_s[2]);
					
					if (serial == -1)
						serial = soa_serial;
					else if (serial != soa_serial)
						for (final String s : moo.conf.getAdminChannels())
							moo.privmsg(s, "DNS: SOA mismatch for " + ns + ": Expected " + serial + " but got " + soa_serial);
				}
				catch (NamingException ex)
				{
					for (final String s : moo.conf.getAdminChannels())
						moo.privmsg(s, "DNS: NamingError checking serial for " + ns + ": " + ex);
					ex.printStackTrace();
				}
				catch (Exception ex)
				{
					for (final String s : moo.conf.getAdminChannels())
						moo.privmsg(s, "DNS: Error checking serial for " + ns + ": " + ex);
					ex.printStackTrace();
				}
			}
		}
		catch (NamingException ex)
		{
			for (final String s : moo.conf.getAdminChannels())
				moo.privmsg(s, "DNS: NamingError checking nameserver serials: " + ex);
			ex.printStackTrace();
		}
		catch (Exception ex)
		{
			for (final String s : moo.conf.getAdminChannels())
				moo.privmsg(s, "DNS: Error checking nameserver serials: " + ex);
			ex.printStackTrace();
		}
		
		for (final String r : regions)
		{
			try
			{
				InetAddress.getByName(r + ".iso." + domain);
			}
			catch (UnknownHostException e)
			{
				for (final String s : moo.conf.getAdminChannels())
					moo.privmsg(s, "DNS: Unable to resolve " + r + ".iso." + domain + ": " + e.getMessage());
			}
		}
	}
}
