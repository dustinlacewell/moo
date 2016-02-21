package net.rizon.moo.plugin.random;

import java.util.Iterator;

class Pattern extends FloodList
{
	private Field type;

	private short length = 0;

	private short lower = 0, upper = 0, number = 0, other = 0;

	/* 0 = lower, 1 = upper */
	private int lowerUpperMask = 0;
	/* 0 = not, 1 = char */
	private int charMask = 0;
	/* 0 = not, 1 = number */
	private int numberMask = 0;

	private Pattern(random random, Field type, final String s)
	{
		super(random);
		
		this.type = type;

		this.length = (short) s.length();

		for (int i = 0; i < this.length; ++i)
		{
			char c = s.charAt(i);

			if (Character.isLetter(c))
			{
				this.charMask |= 1 << i;

				if (Character.isUpperCase(c))
				{
					++this.upper;
					this.lowerUpperMask |= 1 << i;
				}
				else if (Character.isLowerCase(c))
					++this.lower;
			}
			else if (Character.isDigit(c))
			{
				this.numberMask |= 1 << i;

				++this.number;
			}
			else
			{
				++this.other;
			}
		}
	}

	@Override
	public String toString()
	{
		return this.type.name.substring(0,  1) + "/L:" + this.lower + "/U:" + this.upper + "/N:" + this.number + "/O:" + this.other;
	}

	protected boolean equalTo(Pattern other)
	{
		if (this.type.ordinal() != other.type.ordinal())
			return false;
		else if (this.length != other.length)
			return false;
		else if (this.lower != other.lower)
			return false;
		else if (this.upper != other.upper)
			return false;
		else if (this.lowerUpperMask != other.lowerUpperMask)
			return false;
		else if (this.charMask != other.charMask)
			return false;
		else if (this.numberMask != other.numberMask)
			return false;
		else if (this.number != other.number)
			return false;
		else if (this.other != other.other)
			return false;

		return true;
	}

	public static Pattern getOrCreatePattern(random random, Field type, String s)
	{
		Pattern p = new Pattern(random, type, s);
		for (Iterator<FloodList> it = FloodList.getLists().iterator(); it.hasNext();)
		{
			FloodList fl = it.next();

			if (fl.isClosed)
				continue;

			if (fl instanceof Pattern)
			{
				Pattern pfl = (Pattern) fl;

				if (pfl.equalTo(p))
					return pfl;
			}
		}

		p.open();
		return p;
	}
}