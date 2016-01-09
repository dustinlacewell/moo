package net.rizon.moo.plugin.tickets;

/**
 * Enumerator for the ticket system. Matches the states used on the site.
 * <p>
 * @author Orillion {@literal <orillion@rizon.net>}
 */
enum TicketState
{
	UNKNOWN("UNKNOWN"),
	PENDING("PENDING"),
	IN_PROGRESS("IN-PROGRESS"),
	RESOLVED("RESOLVED"),
	CLOSED("CLOSED");

	private final String text;

	TicketState(String text)
	{
		this.text = text;
	}

	/**
	 * Creates a new {@link TicketState} from a string.
	 * <p>
	 * @param text String form, can input site return directly.
	 * <p>
	 * @return State matching the string, or <code>TicketState.UNKNOWN</code>
	 */
	public static TicketState create(String text)
	{
		if (text != null)
		{
			for (TicketState t : TicketState.values())
			{
				if (text.equalsIgnoreCase(t.text))
				{
					return t;
				}
			}
		}

		return UNKNOWN;
	}
}
