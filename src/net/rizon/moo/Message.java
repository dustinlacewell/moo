package net.rizon.moo;

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
	
	public final String getName()
	{
		return this.name;
	}
	
	public abstract void run(final String source, final String[] message);
	
	public static LinkedList<Message> messages = new LinkedList<Message>();
	
	public static void runMessage(final String source, final String message, final String[] buffer)
	{
		for (Iterator<Message> it = messages.iterator(); it.hasNext();)
		{
			Message m = it.next();
			if (m.getName().equalsIgnoreCase(message))
				m.run(source, buffer);
		}
	}
}
