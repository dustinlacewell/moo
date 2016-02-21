package net.rizon.moo.plugin.commands.sid;

import com.google.inject.Inject;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;

public class CommandSidClient extends CommandSidBase
{
	@Inject
	public CommandSidClient(Config conf)
	{
		super(conf, "!SID", "Generates a new server ID");
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !SID");
		source.notice("Generates a new SID for a client server. It will be checked not to be already in use.");
	}

	@Override
	protected String getSID()
	{
		int i = rand.nextInt(100);
		String s = Integer.toString(i);
		if (s.length() == 1)
			s = "0" + s;
		s += "C";
		return s;
	}
}
