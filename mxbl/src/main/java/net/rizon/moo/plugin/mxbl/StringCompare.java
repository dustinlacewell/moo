package net.rizon.moo.plugin.mxbl;

/**
 *
 * @author Orillion <orillion@rizon.net>
 * @author xMythycle
 */
public class StringCompare
{
	/**
	 * Compares a wildcard {@link String} to a standard {@link String}.
	 * <p>
	 * @param wildcard {@link String} containing wildcards.
	 * @param string   {@link String} to compare to comparison.
	 * <p>
	 * @return <code>true</code> if wildcard {@link String} matches the standard
	 *         {@link String}, <code>false</code> otherwise.
	 */
	public static boolean wildcardCompare(String wildcard, String string)
	{
		// do basic checking in case the problem is easy
		if (!(wildcard.contains("?") || wildcard.contains("*")))
		{
			return wildcard.equals(string);
		}

		// turn the entire string into a quoted regex string
		wildcard = "\\Q" + wildcard + "\\E";

		// replace the appropriate symbols with their regex equivalent and make sure to unquote them
		wildcard = wildcard.replaceAll("\\?", "\\\\E.\\\\Q");
		wildcard = wildcard.replaceAll("\\*", "\\\\E.*\\\\Q");

		return string.matches(wildcard);
	}

}
