package net.rizon.moo;

import net.rizon.moo.irc.User;
import com.google.inject.Inject;
import java.util.Set;
import net.rizon.moo.io.IRCMessage;
import net.rizon.moo.irc.IRC;

public class CommandManager
{
	@Inject
	private Set<Command> commands;

	@Inject
	private IRC irc;
	
	private Command find(String name)
	{
		for (Command c : commands)
			if (c.getCommandName().equalsIgnoreCase(name))
				return c;
		return null;
	}
	
	public void run(IRCMessage m)
	{
		String[] message = m.getParams();
		
		if (message.length < 2 || message[0].startsWith("#") == false || (message[1].startsWith("!") == false && message[1].startsWith(".") == false))
			return;

		String tokens[] = message[1].split(" ");
		Command c = find(tokens[0]);
		if (c == null)
			return;

		if (!c.isRequiredChannel(message[0]))
			return;

		User user = irc.findUser(m.getNick());
		if (user == null)
			user = new User(m.getNick());

		CommandSource csource = new CommandSource(user, irc.findChannel(message[0]));

		c.execute(csource, tokens);
	}
}
