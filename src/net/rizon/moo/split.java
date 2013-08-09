package net.rizon.moo;

import java.util.Date;

public class split
{
	public String me, from, to, reconnectedBy;
	public Date when, end;
	
	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof split))
			return false;
		
		split s = (split) other;
		
		return me.equals(s.me) && from.equals(s.from) && when.equals(s.when);
	}
}