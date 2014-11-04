package net.rizon.moo.conf;

public enum Protocol
{
	PLEXUS("plexus"),
	UNREAL("unreal");

	private String name;

	/**
	 * Creates a new Protocol object.
	 * @param name Name of the protocol.
	 */
	Protocol(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the name of this protocol.
	 * @return Name of this protocol.
	 */
	public String getName()
	{
		return this.name;
	}

	/**
	 * Finds a protocol in the list of available protocols.
	 * @param name Name of protocol to find.
	 * @return Protocol if it exists, else null.
	 */
	public static Protocol fromString(String name)
	{
		for (Protocol p : Protocol.values())
			if (name.equalsIgnoreCase(p.name))
				return p;
		return null;
	}
}
