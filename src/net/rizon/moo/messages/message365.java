package net.rizon.moo.messages;

import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.table;

/* End of LINKS */
public class message365 extends message
{
	public message365()
	{
		super("365");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (moo.conf.getDatabase().isEmpty() == false)
			for (table t : table.getTables())
				t.load();
	}
}