package net.rizon.moo.commands;

import java.util.HashSet;

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
		for (server s2 : server.getServers())
		{
			int l = s2.getName().length();
			if (l > longest)
				longest = l;
		}
		
		return longest - s.getName().length() + 2;
	}
	
	@Override
	public void run(String source, String[] msg)
	{
		if (target_channel == null || target_source == null)
			return;
		
		server s = server.findServerAbsolute(source);
		if (s == null)
			s = new server(source);

		if (waiting_for.remove(s.getName()) == false)
			return;

		String tok = msg[1];
		
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
			
			if (commandVersions.onlyOld && ver_num == max_ver)
				return;
			
			String buf = "[VERSION] " + source + " ";
			for (int i = 0, dashes = dashesFor(s); i < dashes; ++i)
				buf += "-";
			buf += " ";
			if (ver_num >= 0 && ver_num < max_ver - 4)
				buf += message.COLOR_RED;
			else if (ver_num >= 0 && ver_num < max_ver - 1)
				buf += message.COLOR_YELLOW;
			else if (ver_num < max_ver)
				buf += message.COLOR_GREEN;
			else
				buf += message.COLOR_BRIGHTGREEN;
			buf += ver + message.COLOR_END;
			
			moo.reply(target_source, target_channel, buf);
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
	
	public static boolean onlyOld;

	public commandVersions(mpackage pkg)
	{
		super(pkg, "!VERSIONS", "View the IRCd versions");
	}

	@Override
	public void onHelp(String source)
	{
		moo.notice(source, "Syntax: !VERSIONS [OLD]");
		moo.notice(source, "This command gets the version of all currently linked IRCds and lists them.");
		moo.notice(source, "If OLD is given as a parameter, only versions that aren't the latest will be shown.");
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length > 1 && params[1].equalsIgnoreCase("OLD"))
			onlyOld = true;
		else
			onlyOld = false;

		for (server s : server.getServers())
			if (s.isServices() == false)
			{
				moo.sock.write("VERSION " + s.getName());
				message351.waiting_for.add(s.getName());
			}
		
		message351.target_channel = target;
		message351.target_source = source;
	}
}
