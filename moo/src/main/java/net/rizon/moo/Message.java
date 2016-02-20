package net.rizon.moo;

import net.rizon.moo.io.IRCMessage;

public abstract class Message
{
	public static final String COLOR_GREEN = "\00303";
	public static final String COLOR_RED = "\00304";
	public static final String COLOR_ORANGE = "\00307";
	public static final String COLOR_YELLOW = "\00308";
	public static final String COLOR_BRIGHTGREEN = "\00309";
	public static final String COLOR_BRIGHTBLUE = "\00311";
	public static final String COLOR_UNDERLINE = "\037";
	public static final String COLOR_END = "\017";

	private String name;

	public Message(final String name)
	{
		this.name = name;
	}

	public final String getName()
	{
		return this.name;
	}

	public abstract void run(IRCMessage message);
}
