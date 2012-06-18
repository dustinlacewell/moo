package net.rizon.moo.random;

import java.util.HashMap;
import java.util.Iterator;

import net.rizon.moo.moo;

public class pattern extends floodList
{
	private boolean isList = false;
	
	private short length = 0;
	
	private short lower = 0, upper = 0, number = 0, other = 0;
	
	/* 0 = lower, 1 = upper */
	private int lowerUpperMask = 0;
	/* 0 = not, 1 = char */
	private int charMask = 0;
	/* 0 = not, 1 = number */
	private int numberMask = 0;
	
	private pattern(final String s)
	{
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
		return "L:" + this.lower + "/U:" + this.upper + "/N:" + this.number + "/O:" + this.other;
	}
	
	@Override
	public int hashCode()
	{
		return this.lowerUpperMask ^ this.charMask ^ this.numberMask;
	}
	
	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof pattern))
			return false;
		
		pattern other = (pattern) obj;
		
		if (this.length != other.length)
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
	
	public void add(nickData nd, final String data)
	{
		this.addMatch(nd);
		if (this.isList)
			random.logMatch(nd, this, data);
		
		long first = this.getTimes().getFirst();
		
		long now = System.currentTimeMillis() / 1000L;
		if (this.isList == false && now - first <= random.timeforMatches && this.getMatches().size() >= random.matchesForFlood && frequency.isRandom(data) && frequency.containsBigrams(data) == false)
		{
			for (int c = 0; c < moo.conf.getFloodChannels().length; ++c)
				moo.privmsg(moo.conf.getFloodChannels()[c], "[FLOOD] Pattern " + this + " detected in incoming clients (" + random.matchesForFlood + " out of last " + random.maxSize + " users), collecting matching users...");
				
			random.addFloodList(this);
			this.isList = true;
				
			for (Iterator<nickData> it = this.getMatches().iterator(); it.hasNext();)
				random.logMatch(it.next(), this, data);
		}
	}
	
	public void del(nickData nd)
	{
		if (this.isList)
			return;
		
		this.delMatch(nd);

		if (this.getMatches().isEmpty())
			patterns.remove(this);
	}
	
	/* hash map of patterns to itself. used because patterns may be .equal() but not ==, and
	 * we need the same reference for each specific pattern to keep a refcount. (and it lowers
	 * memory)
	 */
	private static HashMap<pattern, pattern> patterns = new HashMap<pattern, pattern>();
	
	public static pattern getOrCreatePattern(final String s)
	{
		pattern p = new pattern(s);
		pattern real = patterns.get(p);
		if (real != null)
			return real;
		patterns.put(p, p);
		return p;
	}
	
	public static void removePattern(pattern p)
	{
		patterns.remove(p);
	}
}