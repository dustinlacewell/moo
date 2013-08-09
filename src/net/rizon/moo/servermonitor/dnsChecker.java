package net.rizon.moo.servermonitor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;

import net.rizon.moo.logger;
import net.rizon.moo.moo;

class dnsChecker extends Thread
{
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
			
			Attributes attributes = idir.getAttributes(moo.conf.getServermonitorDomain(), what_array);
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
					
					Attributes nameserver_attributes = idir2.getAttributes(moo.conf.getServermonitorDomain(), what_array);
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
					logger.getGlobalLogger().log(ex);
				}
				catch (Exception ex)
				{
					for (final String s : moo.conf.getAdminChannels())
						moo.privmsg(s, "DNS: Error checking serial for " + ns + ": " + ex);
					logger.getGlobalLogger().log(ex);
				}
				finally
				{
					try { idir2.close(); } catch (Exception ex) { }
				}
			}
		}
		catch (NamingException ex)
		{
			for (final String s : moo.conf.getAdminChannels())
				moo.privmsg(s, "DNS: NamingError checking nameserver serials: " + ex);
			logger.getGlobalLogger().log(ex);
		}
		catch (Exception ex)
		{
			for (final String s : moo.conf.getAdminChannels())
				moo.privmsg(s, "DNS: Error checking nameserver serials: " + ex);
			logger.getGlobalLogger().log(ex);
		}
		finally
		{
			try { idir.close(); } catch (Exception ex) { }
		}
		
		for (final String r : moo.conf.getServermonitorCheck())
		{
			try
			{
				InetAddress.getByName(r);
			}
			catch (UnknownHostException e)
			{
				for (final String s : moo.conf.getAdminChannels())
					moo.privmsg(s, "DNS: Unable to resolve " + r + ": " + e.getMessage());
			}
		}
	}
}
