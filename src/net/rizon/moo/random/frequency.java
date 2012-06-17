package net.rizon.moo.random;

public enum frequency
{
	E('E', (short) 158),
	T('T', (short) 114),
	A('A', (short) 101),
	O('O', (short) 95),
	I('I', (short) 87),
	N('N', (short) 87),
	S('S', (short) 80),
	H('H', (short) 78),
	R('R', (short) 75),
	D('D', (short) 54),
	L('L', (short) 51),
	U('U', (short) 35),
	C('C', (short) 32),
	M('M', (short) 32),
	F('F', (short) 29),
	W('W', (short) 28),
	G('G', (short) 25),
	Y('Y', (short) 24),
	P('P', (short) 22),
	B('B', (short) 19),
	V('V', (short) 12),
	K('K', (short) 9),
	X('X', (short) 2),
	J('J', (short) 1),
	Q('Q', (short) 1),
	Z('Z', (short) 1);
	
	char ch;
	short fr;
	
	private static final short averagePerCharacter = 50;
	
	private static final String[] bigrams = new String[] { "th", "he", "in", "er", "an", "re", "nd", "on", "en", "at", "ou", "ed", "ha", "to", "or", "it", "is", "hi", "es", "ng" };
	
	private frequency(char ch, short fr)
	{
		this.ch = ch;
		this.fr = fr;
	}
	
	private static short getFrequencyScoreFor(char c)
	{
		if (Character.isLetter(c) == false)
			return 0;
		for (frequency f : frequency.values())
			if (f.ch == Character.toUpperCase(c))
				return f.fr;
		return 0;
	}
	
	public static boolean isRandom(final String str)
	{
		short score = 0, total = 0;
		for (int i = 0; i < str.length(); ++i)
		{
			short s = getFrequencyScoreFor(str.charAt(i));
			if (s != 0)
			{
				++total;
				score += s;
			}
		}
		
		short expected = (short) (total * averagePerCharacter);
		return score < expected;
	}
	
	public static boolean containsBigrams(final String str)
	{
		for (final String s : bigrams)
			if (str.contains(s))
				return true;
		return false;
	}
}
