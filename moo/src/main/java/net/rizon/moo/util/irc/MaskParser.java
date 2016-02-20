package net.rizon.moo.util.irc;

public class MaskParser
{
	public static Mask parse(String mask)
	{
		if (!mask.contains("!"))
		{
			if (mask.contains("."))
				return new Mask(null, null, null);
			else
				return new Mask(mask, null, null);
		}

		String[] s = mask.split("!");

		String[] s2 = s[1].split("@");
		return new Mask(s[0], s2[0], s2[1]);
	}
}
