package net.rizon.moo.commands;

import java.util.Iterator;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class commandHelp extends command
{
	public commandHelp(mpackage pkg)
	{
		super(pkg, "!MOO-HELP", "Shows this list");
	}

	@Override
	public void execute(String source, String target, String[] params)
	{
		for (Iterator<mpackage> it = mpackage.getPackages().iterator(); it.hasNext();)
		{
			mpackage pkg = it.next();
			boolean show_header = false;
			
			for (Iterator<command> it2 = pkg.getCommands().iterator(); it2.hasNext();)
			{
				command c = it2.next();
				
				if (c.requiresAdmin() && moo.conf.isAdminChannel(target) == false)
					continue;
				
				if (show_header == false)
				{
					moo.sock.notice(source, pkg.getPackageName() + " - " + pkg.getDescription());
					show_header = true;
				}
				
				c.onHelp(source);
			}
		}
	}
}