package net.rizon.moo;

import java.util.Date;
import java.util.LinkedList;

class database_timer extends timer
{
	public database_timer()
	{
		super(600, true);
		this.start();
	}

	@Override
	public void run(Date now)
	{
		for (table t : table.getTables())
			t.save();
	}
}

public abstract class table
{
	private static LinkedList<table> tables = new LinkedList<table>();
	private static database_timer timer = null;

	protected table()
	{
		tables.add(this);
		if (timer == null)
			timer = new database_timer();
	}
	
	public static table[] getTables()
	{
		table[] a = new table[tables.size()];
		tables.toArray(a);
		return a;
	}

	protected abstract void init();
	public abstract void load();
	public abstract void save();
}
