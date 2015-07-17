package net.rizon.moo;

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
}
