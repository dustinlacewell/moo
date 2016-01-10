package net.rizon.moo.util;

/**
 *
 * @author Orillion {@literal <orillion@rizon.net>}
 */
public class ArgumentParser {
	/**
	 * Parses a time string argument in the form of
	 * <code>\+\d+[d|w|m|M|y]</code>.
	 * <ul>
	 * <li>d - days</li>
	 * <li>w - weeks</li>
	 * <li>m - months</li>
	 * <li>M - months</li>
	 * <li>y - years</li>
	 * </ul>
	 * Example: <code>+3w</code> will return <code>21</code>.
	 * <p>
	 * @param arg Argument to parse.
	 * <p>
	 * @return Number of days. Or {@link IllegalArgumentException} if the
	 *         argument is incorrectly formatted.
	 */
	public static int parseTimeArgumentDays(String arg) throws IllegalArgumentException
	{
		if (arg.charAt(0) != '+')
		{
			throw new IllegalArgumentException("Time argument must start with a +");
		}

		int modifier = 1;
		int days = 0;

		switch (arg.charAt(arg.length() - 1))
		{
			case 'd':
				modifier = 1;
				break;
			case 'w':
				modifier = 7;
				break;
			case 'm':
			case 'M':
				modifier = 31;
				break;
			case 'y':
				modifier = 365;
				break;
			default:
				throw new IllegalArgumentException("Time argument must end with 'd', 'w', 'm', 'M', or 'y'");
		}

		try
		{
			days = Integer.parseInt(arg.substring(1, arg.length() - 1));
		}
		catch (NumberFormatException e)
		{
			throw new IllegalArgumentException("Time argument must be an integer");
		}

		return days * modifier;
	}
}
