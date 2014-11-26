package net.rizon.moo;

import java.util.Date;

public class Split
{
	public String me, from, to, reconnectedBy;
	public Date when, end;
	public boolean recursive;

	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof Split))
			return false;

		Split s = (Split) other;

		return me.equals(s.me) && from.equals(s.from) && when.equals(s.when);
	}
}