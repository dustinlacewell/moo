package net.rizon.moo.plugin.dnsbl;

import net.rizon.moo.plugin.dnsbl.actions.Action;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.rizon.moo.plugin.dnsbl.conf.DnsblConfiguration;

interface DnsblCallback
{
	void onResult(DnsblCheckResult result);
	void onDone();
}

class DnsblChecker implements Runnable
{
	private static SimpleResolver resolver;

	private DnsblCheckTarget target;
	private BlacklistManager rules;
	private List<DnsblCallback> callbacks = new ArrayList<DnsblCallback>();

	public DnsblChecker(DnsblCheckTarget target, BlacklistManager rules)
	{
		this.target = target;
		this.rules = rules;
	}

	public static void load(DnsblConfiguration c)
	{
		try
		{
			resolver = new SimpleResolver(c.resolver);
		}
		catch (UnknownHostException ex)
		{
			dnsbl.log.log(Level.SEVERE, "Unable to create dnsbl resolver", ex);
		}
	}

	public void addCallback(DnsblCallback cb)
	{
		this.callbacks.add(cb);
	}

	public DnsblCheckResult check(Blacklist blacklist)
	{
		Record[] result = this.getDnsblResponse(blacklist);
		if (result == null)
			return null;
		return this.matchRecordsToRules(blacklist, result);
	}

	protected Record[] getDnsblResponse(Blacklist blacklist)
	{
		try
		{
			String lookupHost = this.getLookupHost(blacklist.getName());
			Lookup message = new Lookup(lookupHost, this.target.isIPv6() ? Type.AAAA : Type.A);
			message.setCache(null);
			message.setResolver(resolver);

			Record[] result = message.run();
			if (message.getResult() == Lookup.SUCCESSFUL)
				return result;
		}
		catch (TextParseException ex)
		{
			dnsbl.log.log(Level.WARNING, "Unable to get dnsbl response", ex);
		}

		return null;
	}

	private String getLookupHost(String host)
	{
		StringBuilder sb = new StringBuilder();
		byte[] bytes = this.target.getIP().getAddress();

		if (this.target.isIPv6())
		{
			// Format:
			//   ABCD:EFGH:IJKL:MNOP:QRST:UVWX:YZAB:CDEF ->
			//   F.E.D.C.B.A.Z.Y.X.W.V.U.T.S.R.Q.P.O.N.M.L.K.J.I.H.G.F.E.D.C.B.A.dnsbl.host
			for (int i = bytes.length - 1; i >= 0; i--)
			{
				int first = bytes[i] & 0xF;
				int second = bytes[i] >> 4;
				sb.append(String.format("%X.%X.", first, second));
			}
		}
		else
		{
			// Format:
			//   X.Y.Z.W -> W.Z.Y.X.dnsbl.host
			for (int i = bytes.length - 1; i >= 0; i--)
			{
				sb.append(bytes[i] & 0xFF);
				sb.append(".");
			}
		}

		sb.append(host);
		return sb.toString();
	}

	private DnsblCheckResult matchRecordsToRules(Blacklist blacklist, Record[] result)
	{
		Map<String, List<Action>> actions = new HashMap<String, List<Action>>();
		boolean isAny = false, isSpecific = false;

		if (result.length == 0)
			return null;

		for (Record record : result)
		{
			String response = record.rdataToString();
			if (!actions.containsKey(response))
				actions.put(response, new ArrayList<Action>());

			for (Rule rule : blacklist.getRules())
				if (response.equals(rule.getResponse()))
				{
					if (isAny)
						actions.put(response, new ArrayList<Action>());
					actions.get(response).add(rule.getAction());
					isSpecific = true;
				}
				else if (rule.getResponse() == null && !isSpecific)
				{
					actions.get(response).add(rule.getAction());
					isAny = true;
				}
		}

		if (actions.size() > 0)
		{
			return new DnsblCheckResult(this.target.getIP(), blacklist, actions);
		}

		return null;
	}

	@Override
	public void run()
	{
		for (Blacklist blacklist : this.rules.getBlacklists())
		{
			DnsblCheckResult result = this.check(blacklist);
			if (result != null)
				for (DnsblCallback cb : this.callbacks)
					cb.onResult(result);
		}

		for (DnsblCallback cb : this.callbacks)
			cb.onDone();
	}

	public void runAsynchronous()
	{
		new Thread(this).start();
	}
}
