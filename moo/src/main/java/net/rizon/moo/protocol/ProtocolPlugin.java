package net.rizon.moo.protocol;

import java.util.EnumSet;

import net.rizon.moo.ChannelUserStatus;
import net.rizon.moo.Plugin;

public abstract class ProtocolPlugin extends Plugin
{
	protected ProtocolPlugin(String name, String desc)
	{
		super(name, desc);
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
