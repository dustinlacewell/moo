package net.rizon.moo.plugin.commands;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Logger;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;

import java.util.HashSet;

class message351 extends Message
{
	public message351()
	{
		super("351");
	}

	protected static CommandSource command_source;
	public static HashSet<String> waiting_for = new HashSet<String>();
	private static int max_ver = 0;
	
	private static int dashesFor(Server s)
	{
		int longest = 0;
		for (Server s2 : Server.getServers())
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
		Server s = Server.findServerAbsolute(source);
		if (s == null)
			s = new Server(source);
		
		if (commandVersionsBase.want_server != null && commandVersionsBase.want_server != s)
			return;

		String tok = msg[1];
		
		int pos = 0;
		for (; pos < tok.length() && tok.charAt(pos) != '('; ++pos);
		for (; pos < tok.length() && tok.charAt(pos) != '-'; ++pos);
		++pos;
		
		if (pos >= tok.length())
			return;
		
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
			
			// We might just want to update the max_ver, not run a command
			if (waiting_for.remove(s.getName()) == false)
				return;
			if (command_source == null)
				return;
			
			if (commandVersionsBase.onlyOld && ver_num == max_ver)
				return;
			
			String buf = "[VERSION] " + source + " ";
			for (int i = 0, dashes = dashesFor(s); i < dashes; ++i)
				buf += "-";
			buf += " ";
			if (ver_num >= 0 && ver_num < max_ver - 4)
				buf += Message.COLOR_RED;
			else if (ver_num >= 0 && ver_num < max_ver - 1)
				buf += Message.COLOR_YELLOW;
			else if (ver_num < max_ver)
				buf += Message.COLOR_GREEN;
			else
				buf += Message.COLOR_BRIGHTGREEN;
			buf += ver + Message.COLOR_END;
			
			int serno_pos1;
			for (serno_pos1 = 0; serno_pos1 < tok.length() && tok.charAt(serno_pos1) != '('; ++serno_pos1);
			buf += " (";
			buf += tok.substring(serno_pos1+1, pos-1);
			buf += ")";

			command_source.reply(buf);
		}
		catch (Exception ex)
		{
			Logger.getGlobalLogger().log(ex);
		}
	}
}

class commandVersionsBase extends Command
{
	@SuppressWarnings("unused")
	private static message351 msg_351 = new message351();
	static Server want_server = null;
	
	public static boolean onlyOld;

	public commandVersionsBase(Plugin pkg, final String command)
	{
		super(pkg, command, "View the IRCd versions");
		
		this.requiresChannel(Moo.conf.getList("oper_channels"));
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !VERSIONS [OLD|server]");
		source.notice("This command gets the version and serno of all currently linked IRCds and lists them.");
		source.notice("If OLD is given as a parameter, only versions that aren't the latest will be shown.");
		source.notice("If a server name is given, the version for that server will be shown.");
	}
	
	@Override
	public void execute(CommandSource source, String[] params)
	{
		if (params.length > 1)
		{
			if (params[1].equalsIgnoreCase("OLD"))
			{
				onlyOld = true;
				want_server = null;
			}
			else
			{
				onlyOld = false;
				want_server = Server.findServer(params[1]);
			}
		}
		else
			want_server = null;

		for (Server s : Server.getServers())
		{
			if (s.isServices() == false)
			{
				Moo.sock.write("VERSION " + s.getName());
				message351.waiting_for.add(s.getName());
			}
		}

		message351.command_source = source;
	}
}

class CommandVersions
{
	private Command vs, v;
	
	public CommandVersions(Plugin pkg)
	{
		vs = new commandVersionsBase(pkg, "!VERSIONS");
		// Some people can't type for their life...
		v = new commandVersionsBase(pkg, "!VERSION");
	}
	
	public void remove()
	{
		vs.remove();
		v.remove();
	}
}