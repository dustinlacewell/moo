package net.rizon.moo;

import net.rizon.moo.io.IRCMessage;
import java.util.Iterator;
import java.util.LinkedList;

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
		Message.messages.push(this);
	}

	public void remove()
	{
		Message.messages.remove(this);
	}

	public final String getName()
	{
		return this.name;
	}

	public abstract void run(final String source, final String[] message);

	public static LinkedList<Message> messages = new LinkedList<Message>();

	public static void runMessage(IRCMessage message)
	{
		int hash = messages.hashCode(); // XXX
		for (Iterator<Message> it = messages.iterator(); it.hasNext() && hash == messages.hashCode();)
		{
			Message m = it.next();
			if (m.getName().equalsIgnoreCase(message.getCommand()))
				m.run(message.getSource(), message.getParams());
		}
	}
}
