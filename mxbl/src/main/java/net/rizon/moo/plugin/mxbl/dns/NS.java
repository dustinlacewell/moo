package net.rizon.moo.plugin.mxbl.dns;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Orillion <orillion@rizon.net>
 */
public class NS
{
	private static final Logger logger = LoggerFactory.getLogger(NS.class);

	/**
	 * Looks up the specified {@link RecordType}s for the given hostname.
	 * <p>
	 * @param hostname Hostname to look up.
	 * @param types    {@link RecordType}s to look up.
	 * <p>
	 * @return List of NameServer replies.
	 * <p>
	 */
	public static HashMap<RecordType, List<String>> lookup(String hostname, List<RecordType> types)
	{
		HashMap<RecordType, List<String>> m = new HashMap<RecordType, List<String>>();
		try
		{
			Hashtable env = new Hashtable();
			env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
			DirContext ictx = new InitialDirContext(env);
			String[] s = new String[types.size()];
			for (int i = 0; i < types.size(); i++)
			{
				s[i] = types.get(i).toString();
			}
			Attributes attrs = ictx.getAttributes(hostname, s);

			for (RecordType r : types)
			{
				List<String> records = processRecords(attrs.get(r.toString()));
				m.put(r, records);
			}

			return m;
		}
		catch (NamingException ex)
		{
			return null;
		}
		catch (UnknownHostException ex)
		{
			return null;
		}
	}

	private static List<String> processRecords(Attribute attr) throws UnknownHostException
	{
		ArrayList<String> list = new ArrayList<String>();
		if (attr == null)
		{
			return list;
		}
		for (int i = 0; i < attr.size(); i++)
		{
			try
			{
				Object o = attr.get(i);
				if (o instanceof String)
				{
					list.add((String) attr.get(i));
				}
			}
			catch (NamingException ex)
			{
				logger.info("Naming Exception: ", ex.getMessage());
			}
		}
		return list;
	}
}
