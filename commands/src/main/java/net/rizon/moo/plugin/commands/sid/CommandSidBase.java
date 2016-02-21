package net.rizon.moo.plugin.commands.sid;

import com.google.inject.Inject;
import java.util.Random;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;

abstract class CommandSidBase extends Command
{
	@Inject
	private ServerManager serverManager;

	protected CommandSidBase(Config conf, String name, String desc)
	{
		super(name, desc);
		this.requiresChannel(conf.admin_channels);
	}

	private boolean inUse(final String sid)
	{
		for (Server s : serverManager.getServers())
			if (s.getSID() != null && s.getSID().equalsIgnoreCase(sid))
				return true;

		return false;
	}

	protected static final Random rand  = new Random();

	protected abstract String getSID();

	@Override
	public void execute(CommandSource source, String[] params)
	{
		String sid;

		do
			sid = getSID();
		while (inUse(sid));

		source.reply("[SID] " + sid);
	}
}