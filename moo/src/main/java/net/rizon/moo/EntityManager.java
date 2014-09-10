package net.rizon.moo;

import java.util.HashMap;

public abstract class EntityManager<T extends Nameable>
{
	protected final HashMap<String, T> entities = new HashMap<String, T>();

	public T find(String name)
	{
		/* We're intentionally ignoring the case mapping issues around IRC for simplicity here. */
		return this.entities.get(name.toLowerCase());
	}

	public void add(T e)
	{
		this.entities.put(e.getName().toLowerCase(), e);
	}

	public void remove(T e)
	{
		this.entities.remove(e.getName().toLowerCase());
	}
}
