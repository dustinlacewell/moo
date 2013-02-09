package net.rizon.moo.fun;

import net.rizon.moo.mpackage;

public class fun extends mpackage
{
	public fun()
	{
		super("FUN", "Provides fun features");
		
		new commandRizonTime(this);
		new eventFun();
	}
}