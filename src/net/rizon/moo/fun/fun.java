package net.rizon.moo.fun;

import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class fun extends mpackage
{
	public fun()
	{
		super("FUN", "Provides fun features");
		
		if (moo.conf.getProtocol().equals("plexus"))
		{
			new commandRizonTime(this);
			new eventFun();
		}
	}
}