package net.rizon.moo;

public abstract class command extends message
{
	private String cmdname;

	public command(final String cmdname)
	{
		super("PRIVMSG");
		this.cmdname = cmdname;;
	}

	@Override
	public void run(String source, String[] message)
	{
		if (message.length < 2 || message[0].startsWith("#") == false || message[1].startsWith("!") == false)
			return;
		
		String tokens[] = message[1].substring(1).split(" ");
		if (this.cmdname.equalsIgnoreCase(tokens[0]))
			this.execute(source, message[0], tokens);
	}
	
	public abstract void execute(final String source, final String target, final String[] params);
}
