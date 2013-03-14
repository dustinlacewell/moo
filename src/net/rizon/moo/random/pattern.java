package net.rizon.moo.random;

import java.util.HashMap;

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
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof pattern))
			return false;
		
		pattern other = (pattern) obj;
		
		if (this.type != other.type)
			return false;
		else if (this.length != other.length)
			return false;
		else if (this.lower != other.lower)
			return false;
		else if (this.upper != other.upper)
			return false;
		else if (this.number != other.number)
			return false;
		else if (this.other != other.other)
			return false;
		
		return true;
	}
	
	@Override
	public void onClose()
	{
		removePattern(this);
	}
	
	/* hash map of patterns to itself. used because patterns may be .equal() but not ==, and
	 * we need the same reference for each specific pattern to keep a refcount. (and it lowers
	 * memory)
	 */
	private static HashMap<pattern, pattern> patterns = new HashMap<pattern, pattern>();
	
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
	
	private static void removePattern(pattern p)
	{
		patterns.remove(p);
	}
}