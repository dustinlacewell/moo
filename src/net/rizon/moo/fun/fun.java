package net.rizon.moo.fun;

import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

public class fun extends MPackage
{
	public fun()
	{
		super("FUN", "Provides fun features");
		
		if (Moo.conf.getProtocol().equals("plexus"))
		{
			new CommandRizonTime(this);
			new EventFun();
		}
	}
}