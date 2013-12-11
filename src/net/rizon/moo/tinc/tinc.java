package net.rizon.moo.tinc;

import net.rizon.moo.MPackage;

public class tinc extends MPackage
{
	public tinc()
	{
		super("tinc", "Manages tinc");
		
		new CommandTinc(this);
	}
	
	public static final String tincBase = ".tinc";
	protected static Layer[] Layers = new Layer[] { new Layer("tl"), new Layer("rc"), new Layer("rh") };
}