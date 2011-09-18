package net.rizon.moo;

import java.util.LinkedList;

public abstract class command extends message
{
	private String cmdname;
	private String description;
	private boolean requireAdmin;

	public command(final String cmdname, final String desc)
	{
		super("PRIVMSG");
		this.cmdname = cmdname;
		this.description = desc;
		commands.add(this);
	}
	
	public final String getCommandName()
	{
		return this.cmdname;
	}
	
	public final String getDescription()
	{
		return this.description;
	}
	
	protected void requireAdmin()
	{
		this.requireAdmin = true;
	}
	
	public final boolean requiresAdmin()
	{
		return this.requireAdmin;
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2 || message[1].startsWith("!") == false)
			return;
		else if (this.requiresAdmin() && moo.conf.isAdminChannel(message[0]) == false)
			return;
		
		String tokens[] = message[1].split(" ");
		if (this.cmdname.equalsIgnoreCase(tokens[0]))
			this.execute(source, message[0], tokens);
	}
	
	public abstract void execute(final String source, final String target, final String[] params);
	
	public void onHelp(final String source)
	{
		if (this.getDescription() != null && this.getDescription().isEmpty() == false)
			moo.sock.notice(source, this.getCommandName() + " - " + this.getDescription());
		else
			moo.sock.notice(source, this.getCommandName());
	}
	
	private static LinkedList<command> commands = new LinkedList<command>();

	public static final LinkedList<command> getCommands()
	{
		return commands;
	}
}
