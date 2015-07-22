package net.rizon.moo.io;

public class IRCMessage
{
	private final String source;
	private final String command;
	private final String[] params;

	public IRCMessage(String source, String command, String[] params)
	{
		this.source = source;
		this.command = command;
		this.params = params;
	}

	public String getSource()
	{
		return source;
	}

	public String getCommand()
	{
		return command;
	}

	public String[] getParams()
	{
		return params;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		
		if (source != null)
			sb.append('[').append(source).append("] ");
		
		sb.append(command);
		
		for (int i = 0; i < params.length; ++i)
		{
			sb.append(' ');
			if (i + 1 == params.length)
				sb.append(':');
			sb.append(params[i]);
		}
		
		return sb.toString();
	}
}
