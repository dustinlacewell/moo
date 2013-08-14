package net.rizon.moo.tinc;

import net.rizon.moo.mpackage;

public class tinc extends mpackage
{
	public tinc()
	{
		super("tinc", "Manages tinc");
		
		new commandTinc(this);
	}
	
	public static final String tincBase = ".tinc";
	protected static layer[] Layers = new layer[] { new layer("tl"), new layer("rc"), new layer("rh") };
}