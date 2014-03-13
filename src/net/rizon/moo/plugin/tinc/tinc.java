package net.rizon.moo.plugin.tinc;

import net.rizon.moo.Command;
import net.rizon.moo.Plugin;

public class tinc extends Plugin
{
	public static final String tincBase = ".tinc";
	protected static Layer[] Layers = new Layer[] { new Layer("tl"), new Layer("rc"), new Layer("rh") };
	
	private Command tinc;
	
	public tinc()
	{
		super("tinc", "Manages tinc");
	}
	
	@Override
	public void start() throws Exception
	{
		tinc = new CommandTinc(this);
	}
	
	@Override
	public void stop()
	{
		tinc.remove();
	}
}