package net.rizon.moo.logging;

import net.rizon.moo.MPackage;

public class logging extends MPackage
{
	public logging()
	{
		super("Logging", "Search server logs");
		
		new CommandLogSearch(this);
		new CommandSLogSearch(this);
		
		new EventLogging();
	}
}
