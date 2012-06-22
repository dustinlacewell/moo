package net.rizon.moo.random;

public enum field
{
	FIELD_NICK("Nick"),
	FIELD_IDENT("Ident"),
	FIELD_GECOS("Gecos");
	
	String name;
	
	private field(final String name)
	{
		this.name = name;
	}
}