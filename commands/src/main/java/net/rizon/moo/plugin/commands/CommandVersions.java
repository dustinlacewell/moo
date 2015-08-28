package net.rizon.moo.plugin.commands;

import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class message351 extends Message
{
	public message351()
	{
		super("351");
	}

	protected static CommandSource command_source;
	public static HashSet<String> waiting_for = new HashSet<String>();
	private static Map<Integer, Integer> max_vers = new HashMap<>();

	final class ServerVersion
	{
		public final int major;
		public final int release;

		public ServerVersion(int major_version, int release)
		{
			this.major = major_version;
			this.release = release;
		}
	}

	private ServerVersion splitVersion(int ver)
	{
		int major_version = ver / 100;
		int release = ver % 100;

		return new ServerVersion(major_version, release);
	}

	private void updateMaxVersion(int ver)
	{
		ServerVersion version = splitVersion(ver);

		if ((!max_vers.containsKey(version.major)) || (max_vers.get(version.major) < version.release))
			max_vers.put(version.major, version.release);

	}

	private boolean isMaxVersion(int ver)
	{
		ServerVersion version = splitVersion(ver);

		if (!max_vers.containsKey(version.major))
			return true;
		else if (version.release >= max_vers.get(version.major))
			return true;
		else
			return false;
	}

	private String versionColor(int ver)
	{
		ServerVersion version = splitVersion(ver);
		int rel = version.release;
		int max_rel = max_vers.get(version.major);

		String color;

		if (rel >= 0 && rel < max_rel - 4)
			color = Message.COLOR_RED;
		else if (rel >= 0 && rel < max_rel - 1)
			color = Message.COLOR_YELLOW;
		else if (rel < max_rel)
			color = Message.COLOR_GREEN;
		else
			color = Message.COLOR_BRIGHTGREEN;

		return color;
	}

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

		int pos = tok.length() - 1;
		for (; pos > 0 && tok.charAt(pos) != '('; --pos);
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
				updateMaxVersion(ver_num);
			}
			catch (NumberFormatException ex) { }

			// We might just want to update the max version, not run a command
			if (waiting_for.remove(s.getName()) == false)
				return;
			if (command_source == null)
				return;

			if (commandVersionsBase.onlyOld && isMaxVersion(ver_num))
				return;

			String buf = "[VERSION] " + source + " ";
			for (int i = 0, dashes = dashesFor(s); i < dashes; ++i)
				buf += "-";
			buf += " ";
			buf += versionColor(ver_num);
			buf += ver + Message.COLOR_END;

			int serno_pos1;
			for (serno_pos1 = tok.length() - 1; serno_pos1 > 0 && tok.charAt(serno_pos1) != '('; --serno_pos1);
			buf += " (";
			buf += tok.substring(serno_pos1+1, pos-1);
			buf += ")";

			command_source.reply(buf);
		}
		catch (Exception ex)
		{
			commandVersionsBase.logger.warn("Unable to parse 351", ex);
		}
	}
}

class commandVersionsBase extends Command
{
	static final Logger logger = LoggerFactory.getLogger(commandVersionsBase.class);
	
	@SuppressWarnings("unused")
	private static message351 msg_351 = new message351();
	static Server want_server = null;

	public static boolean onlyOld;

	public commandVersionsBase(Plugin pkg, final String command)
	{
		super(pkg, command, "View the IRCd versions");

		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
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
				Moo.write("VERSION", s.getName());
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