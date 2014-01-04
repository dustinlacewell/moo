package net.rizon.moo;

public abstract class Command extends Message
{
	private Plugin pkg;
	private String cmdname;
	private String description;
	private String[] requiresChannel;

	public Command(Plugin pkg, final String cmdname, final String desc)
	{
		super("PRIVMSG");
		this.pkg = pkg;
		this.cmdname = cmdname;
		this.description = desc;
		
		pkg.commands.add(this);
	}
	
	@Override
	public void remove()
	{
		pkg.commands.remove(this);
		super.remove();
	}
	
	public Plugin getPackage()
	{
		return this.pkg;
	}
	
	public final String getCommandName()
	{
		return this.cmdname;
	}
	
	public final String getDescription()
	{
		return this.description;
	}
	
	protected void requiresChannel(final String[] channels)
	{
		this.requiresChannel = channels;
	}
	
	public boolean isRequiredChannel(String channel)
	{
		if (this.requiresChannel == null)
			return true;
		
		for (String s : this.requiresChannel)
			if (s.equalsIgnoreCase(channel))
				return true;
		return false;
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2 || message[0].startsWith("#") == false || (message[1].startsWith("!") == false && message[1].startsWith(".") == false))
			return;
		
		if (!this.isRequiredChannel(message[0]))
			return;
		
		String tokens[] = message[1].split(" ");
		if (this.cmdname.equalsIgnoreCase(tokens[0]))
			this.execute(source, message[0], tokens);
	}
	
	public abstract void execute(final String source, final String target, final String[] params);
	
	public void onHelpList(final String source)
	{
		if (this.getDescription() != null && this.getDescription().isEmpty() == false)
			Moo.notice(source, " " + this.getCommandName() + " - " + this.getDescription());
		else
			Moo.notice(source, " " + this.getCommandName());
	}
	
	public void onHelp(final String source)
	{
		Moo.notice(source, "No help available.");
	}
}
