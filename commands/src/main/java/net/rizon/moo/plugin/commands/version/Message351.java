package net.rizon.moo.plugin.commands.version;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Message;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import org.slf4j.Logger;

public class Message351 extends Message
{
	@Inject
	private static Logger logger;
	
	@Inject
	private ServerManager serverManager;
	
	public Message351()
	{
		super("351");
	}

	protected static CommandSource command_source;
	public static Set<String> waiting_for = new HashSet<>();
	private static Map<Integer, Integer> max_vers = new HashMap<>();
	private int max_major = 0;

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

	private void updateMaxMajor(int ver)
	{
		ServerVersion version = splitVersion(ver);
		max_major = Math.max(version.major, max_major);
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

		if (version.major < max_major || rel >= 0 && rel < max_rel - 4)
			color = Message.COLOR_RED;
		else if (rel >= 0 && rel < max_rel - 1)
			color = Message.COLOR_YELLOW;
		else if (rel < max_rel)
			color = Message.COLOR_GREEN;
		else
			color = Message.COLOR_BRIGHTGREEN;

		return color;
	}

	private int dashesFor(Server s)
	{
		int longest = 0;
		for (Server s2 : serverManager.getServers())
		{
			int l = s2.getName().length();
			if (l > longest)
				longest = l;
		}

		return longest - s.getName().length() + 2;
	}

	@Override
	public void run(IRCMessage message)
	{
		Server s = serverManager.findServerAbsolute(message.getSource());
		if (s == null)
		{
			s = new Server(message.getSource());
			serverManager.insertServer(s);
		}

		if (CommandVersionBase.want_server != null && CommandVersionBase.want_server != s)
			return;

		String tok = message.getParams()[1];

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
				updateMaxMajor(ver_num);
			}
			catch (NumberFormatException ex) { }

			// We might just want to update the max version, not run a command
			if (waiting_for.remove(s.getName()) == false)
				return;
			if (command_source == null)
				return;

			if (CommandVersionBase.onlyOld && isMaxVersion(ver_num))
				return;

			String buf = "[VERSION] " + message.getSource() + " ";
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
			logger.warn("Unable to parse 351", ex);
		}
	}
}
