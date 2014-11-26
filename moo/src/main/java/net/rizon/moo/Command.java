package net.rizon.moo;

import java.util.ArrayList;

public abstract class Command extends Message
{
	private Plugin pkg;
	private String cmdname;
	private String description;
	private ArrayList<String> channels = new ArrayList<String>();

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

	protected void requiresChannel(final String[] chans)
	{
		for (String c : chans)
			channels.add(c.toLowerCase());
	}

	public boolean isRequiredChannel(String channel)
	{
		return channels.isEmpty() || channels.contains(channel.toLowerCase());
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2 || message[0].startsWith("#") == false || (message[1].startsWith("!") == false && message[1].startsWith(".") == false))
			return;

		String tokens[] = message[1].split(" ");
		if (!this.cmdname.equalsIgnoreCase(tokens[0]))
			return;

		if (!this.isRequiredChannel(message[0]))
			return;

		User user = Moo.users.findOrCreateUser(source);
		CommandSource csource = new CommandSource(user, Moo.channels.find(message[0]));

		this.execute(csource, tokens);

		if (user.getChannels().isEmpty())
			Moo.users.remove(user);
	}

	public abstract void execute(CommandSource source, String[] params);

	public void onHelpList(CommandSource source)
	{
		if (this.getDescription() != null && this.getDescription().isEmpty() == false)
			source.notice(" " + this.getCommandName() + " - " + this.getDescription());
		else
			source.notice(" " + this.getCommandName());
	}

	public void onHelp(CommandSource source)
	{
		source.notice("No help available.");
	}
}
