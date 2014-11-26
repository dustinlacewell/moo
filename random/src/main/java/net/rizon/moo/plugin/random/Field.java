package net.rizon.moo.plugin.random;

enum Field
{
	FIELD_NICK("Nick"),
	FIELD_IDENT("Ident"),
	FIELD_GECOS("Gecos");

	String name;

	private Field(final String name)
	{
		this.name = name;
	}
}