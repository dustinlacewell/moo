package net.rizon.moo;

import java.util.Arrays;

public abstract class command extends message
{
	private mpackage pkg;
	private String cmdname;
	private String description;
	private String[] requiresChannel;

	public command(mpackage pkg, final String cmdname, final String desc)
	{
		super("PRIVMSG");
		this.pkg = pkg;
		this.cmdname = cmdname;
		this.description = desc;
		pkg.addCommand(this);
	}
	
	public mpackage getPackage()
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
	
	public final String[] getRequiredChannels()
	{
		return this.requiresChannel;
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2 || message[0].startsWith("#") == false || (message[1].startsWith("!") == false && message[1].startsWith(".") == false))
			return;
		else if (moo.conf.isIdleChannel(message[0]))
			return;
		else if (this.getRequiredChannels() != null && Arrays.asList(this.getRequiredChannels()).contains(message[0]) == false)
			return;
		
		String tokens[] = message[1].split(" ");
		if (this.cmdname.equalsIgnoreCase(tokens[0]))
			this.execute(source, message[0], tokens);
	}
	
	public abstract void execute(final String source, final String target, final String[] params);
	
	public void onHelpList(final String source)
	{
		if (this.getDescription() != null && this.getDescription().isEmpty() == false)
			moo.notice(source, " " + this.getCommandName() + " - " + this.getDescription());
		else
			moo.notice(source, " " + this.getCommandName());
	}
	
	public void onHelp(final String source)
	{
		moo.notice(source, "No help available.");
	}
}
