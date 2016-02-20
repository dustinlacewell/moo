package net.rizon.moo;

import net.rizon.moo.irc.User;
import com.google.inject.Inject;
import java.util.Set;

public class CommandManager
{
	private final Set<Command> commands;
	
	@Inject
	CommandManager(Set<Command> commands)
	{
		this.commands = commands;
	}
	
	private Command find(String name)
	{
		for (Command c : commands)
			if (c.getCommandName().equalsIgnoreCase(name))
				return c;
		return null;
	}
	
	public void run(String source, String[] message)
	{
		if (message.length < 2 || message[0].startsWith("#") == false || (message[1].startsWith("!") == false && message[1].startsWith(".") == false))
			return;

		String tokens[] = message[1].split(" ");
		Command c = find(tokens[0]);
		if (c == null)
			return;

		if (!c.isRequiredChannel(message[0]))
			return;

		User user = Moo.users.findOrCreateUser(source);
		CommandSource csource = new CommandSource(user, Moo.channels.find(message[0]));

		c.execute(csource, tokens);

		if (user.getChannels().isEmpty())
			Moo.users.remove(user);
	}
}
