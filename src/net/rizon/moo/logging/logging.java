package net.rizon.moo.logging;

import net.rizon.moo.mpackage;

public class logging extends mpackage
{
	public logging()
	{
		super("Logging", "Search server logs");
		
		new commandLogSearch(this);
		new commandSLogSearch(this);
		
		new eventLogging();
	}
}
