package net.rizon.moo.random;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

class patternComparator implements Comparator<pattern>
{
	@Override
	public int compare(pattern arg0, pattern arg1)
	{
		if (arg0.lessThan(arg1))
			return -1;
		else if (arg1.lessThan(arg0))
			return 1;
		else
			return 0;
	}
}

class pattern extends floodList
{
	private field type;
	
	private short length = 0;
	
	private short lower = 0, upper = 0, number = 0, other = 0;
	
	/* 0 = lower, 1 = upper */
	private int lowerUpperMask = 0;
	/* 0 = not, 1 = char */
	private int charMask = 0;
	/* 0 = not, 1 = number */
	private int numberMask = 0;
	
	private pattern(field type, final String s)
	{
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
	
	@Override
	public int hashCode()
	{
		return this.lowerUpperMask ^ this.charMask ^ this.numberMask ^ this.type.toString().hashCode();
	}
	
	protected boolean lessThan(pattern other)
	{
		if (this.type.ordinal() < other.type.ordinal())
			return true;
		else if (this.length < other.length)
			return true;
		else if (this.lower < other.lower)
			return true;
		else if (this.upper < other.upper)
			return true;
		else if (this.number < other.number)
			return true;
		else if (this.other < other.other)
			return true;
		
		return false;
	}
	
	@Override
	public void onClose()
	{
		patterns.remove(this);
	}
	
	private static Map<pattern, pattern> patterns = new TreeMap<pattern, pattern>(new patternComparator());
	
	public static pattern[] getPatterns()
	{
		pattern[] a = new pattern[patterns.size()];
		patterns.keySet().toArray(a);
		return a;
	}
	
	public static pattern getOrCreatePattern(final field type, final String s)
	{
		pattern p = new pattern(type, s);
		pattern real = patterns.get(p);
		if (real != null)
			return real;
		p.open();
		patterns.put(p, p);
		return p;
	}
}