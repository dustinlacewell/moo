package net.rizon.moo.irc;

import com.google.inject.Inject;
import java.util.EnumSet;
import net.rizon.moo.Moo;
import static net.rizon.moo.Moo.akillServ;
import static net.rizon.moo.Moo.conf;
import net.rizon.moo.io.IRCMessage;

public class Protocol
{
	@Inject
	private io.netty.channel.Channel channel;
	
	public void write(String command, Object... args)
	{
		if (channel == null)
			return;
		
		String[] params = new String[args.length];
		int i = 0;
		for (Object o : args)
			params[i++] = o.toString();
		
		IRCMessage message = new IRCMessage(null, command, params);
		channel.writeAndFlush(message);
	}
	
	public void handshake()
	{
		if (conf.general.server_pass != null)
			write("PASS", conf.general.server_pass);

		write("USER", conf.general.ident, ".", ".", conf.general.realname);
		write("NICK", conf.general.nick);

		write("PROTOCTL", "UHNAMES NAMESX");
	}

	public void privmsg(String target, String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		write("PRIVMSG", target, buffer);
	}

	/**
	 * Sends the same message to all targets.
	 * @param targets Array of targets.
	 * @param buffer Message to send.
	 */
	public void privmsgAll(String[] targets, String buffer)
	{
		for (String s : targets)
			privmsg(s, buffer);
	}

	public void notice(String target, String buffer)
	{
		int ex = target.indexOf('!');
		if (ex != -1)
			target = target.substring(0, ex);
		write("NOTICE", target, buffer);
	}

	public void reply(String source, String target, String buffer)
	{
		if (target.equalsIgnoreCase(Moo.conf.general.nick))
			notice(source, buffer);
		else
			privmsg(target, buffer);
	}

	public void join(String target)
	{
		write("JOIN", target);
	}

	public void kick(String target, String channel, String reason)
	{
		write("KICK", channel, target, reason);
	}

	public void mode(String target, String modes)
	{
		String str = target + " " + modes;
		write("MODE", (Object[]) str.split(" "));
	}

	public void kill(String nick, final String reason)
	{
		write("KILL", nick, reason);
	}

	public void akill(String host, String time, String reason)
	{
		if (host.equals("255.255.255.255"))
			return;

		privmsg(akillServ, "AKILL ADD " + time + " *@" + host + " " + reason);
	}

	public void qakill(String nick, String reason)
	{
		privmsg(akillServ, "QAKILL " + nick + " " + reason);
	}

	public void capture(String nick)
	{
		privmsg("RootServ", "CAPTURE " + nick); // XXX
	}

	public void operwall(String message)
	{
		write("OPERWALL", message);
	}
	
	
	public boolean isCUSDisplayCharacter(char c)
	{
		switch (c)
		{
			case '~':
			case '&':
			case '@':
			case '%':
			case '+':
				return true;
			default:
				return false;
		}
	}

	public ChannelUserStatus modeToCUS(char c)
	{
		switch (c)
		{
			case 'q':
				return ChannelUserStatus.OWNER;
			case 'a':
				return ChannelUserStatus.ADMIN;
			case 'o':
				return ChannelUserStatus.OP;
			case 'h':
				return ChannelUserStatus.HALFOP;
			case 'v':
				return ChannelUserStatus.VOICE;
			default:
				return null;
		}
	}

	public ChannelUserStatus CUSDisplayCharacterToCUS(char c)
	{
		switch (c)
		{
			case '~':
				return ChannelUserStatus.OWNER;
			case '&':
				return ChannelUserStatus.ADMIN;
			case '@':
				return ChannelUserStatus.OP;
			case '%':
				return ChannelUserStatus.HALFOP;
			case '+':
				return ChannelUserStatus.VOICE;
			default:
				return null;
		}
	}

	public EnumSet<ChannelUserStatus> CUSDisplayCharacterToEnumSet(final String chars)
	{
		EnumSet<ChannelUserStatus> cus = EnumSet.noneOf(ChannelUserStatus.class);

		for (char c : chars.toCharArray())
			cus.add(CUSDisplayCharacterToCUS(c));

		return cus;
	}
}
