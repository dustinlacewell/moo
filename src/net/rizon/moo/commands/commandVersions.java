package net.rizon.moo.commands;

import java.util.HashSet;
import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

class message351 extends message
{
	public message351()
	{
		super("351");
	}
	
	public static HashSet<String> waiting_for = new HashSet<String>();
	public static String target_channel = null;
	public static String target_source = null;
	private static int max_ver = 0;
	
	private static int dashesFor(server s)
	{
		int longest = 0;
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			int l = it.next().getName().length();
			if (l > longest)
				longest = l;
		}
		
		return longest - s.getName().length() + 2;
	}

	@Override
	public void run(String source, String[] message)
	{
		if (target_channel == null || target_source == null)
			return;
		
		server s = server.findServerAbsolute(source);
		if (s == null)
			s = new server(source);

		if (waiting_for.remove(s.getName()) == false)
			return;

		String tok = message[1];
		
		int pos = 0;
		for (; pos < tok.length() && tok.charAt(pos) != '('; ++pos);
		for (; pos < tok.length() && tok.charAt(pos) != '-'; ++pos);
		++pos;
		
		try
		{
			String ver = tok.substring(pos, tok.length() - 2);
			int ver_num = -1;
			try
			{
				ver_num = Integer.parseInt(ver);
				if (ver_num > max_ver)
					max_ver = ver_num;
			}
			catch (NumberFormatException ex) { }
			
			String buf = "[VERSION] " + source + " ";
			for (int i = 0, dashes = dashesFor(s); i < dashes; ++i)
				buf += "-";
			buf += " \003";
			if (ver_num >= 0 && ver_num < max_ver - 4)
				buf += "04";
			else if (ver_num >= 0 && ver_num < max_ver - 1)
				buf += "08";
			else if (ver_num < max_ver)
				buf += "03";
			else
				buf += "09";
			buf += ver + "\003";
			
			moo.sock.reply(target_source, target_channel, buf);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
	}
}

public class commandVersions extends command
{
	@SuppressWarnings("unused")
	private static message351 msg_351 = new message351();

	public commandVersions(mpackage pkg)
	{
		super(pkg, "!VERSIONS", "View the IRCd versions");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
		{
			server s = it.next();
			
			if (s.isServices() == false)
			{
				moo.sock.write("VERSION " + s.getName());
				message351.waiting_for.add(s.getName());
			}
		}
		
		message351.target_channel = target;
		message351.target_source = source;
	}
}
