package net.rizon.moo;

import java.util.Iterator;
import java.util.LinkedList;

public abstract class message
{
	public static final String COLOR_GREEN = "\00303";
	public static final String COLOR_RED = "\00304";
	public static final String COLOR_ORANGE = "\00307";
	public static final String COLOR_YELLOW = "\00308";
	public static final String COLOR_BRIGHTBLUE = "\00311";
	public static final String COLOR_END = "\017";
	
	private String name;

	public message(final String name)
	{
		this.name = name;
		message.messages.push(this);
	}
	
	public final String getName()
	{
		return this.name;
	}
	
	public abstract void run(final String source, final String[] message);
	
	public static LinkedList<message> messages = new LinkedList<message>();
	
	public static void runMessage(final String source, final String message, final String[] buffer)
	{
		for (Iterator<message> it = messages.iterator(); it.hasNext();)
		{
			message m = it.next();
			if (m.getName().equalsIgnoreCase(message))
				m.run(source, buffer);
		}
	}
}
