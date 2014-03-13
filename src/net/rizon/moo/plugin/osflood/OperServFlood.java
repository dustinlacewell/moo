package net.rizon.moo.plugin.osflood;

import java.util.Date;

class OperServFlood
{
	public final Date start;
	public int frequency;

	public OperServFlood(Date start, int frequency)
	{
		this.start = start;
		this.frequency = frequency;
	}
}
