package net.rizon.moo.plugin.commands.sid;

import com.google.inject.Inject;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;

public class CommandSidHub extends CommandSidBase
{
	@Inject
	public CommandSidHub(Config conf)
	{
		super(conf, "!HUBSID", "Generates a new hub server ID");
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: !HUBSID");
		source.notice("Generates a new SID for a hub. It will be checked not to be already in use.");
	}

	@Override
	protected String getSID()
	{
		int i = rand.nextInt(100);
		String s = Integer.toString(i);
		if (s.length() == 1)
			s = "0" + s;
		s += "H";
		return s;
	}
}