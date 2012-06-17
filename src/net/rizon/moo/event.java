package net.rizon.moo;

import java.util.LinkedList;

public abstract class event
{
	public event()
	{
		events.add(this);
	}
	
	public void onConnect() { }
	
	protected void initDatabases() { }
	public void loadDatabases() { }
	public void saveDatabases() { }
	
	public void onServerCreate(server serv) { }
	public void onServerDestroy(server serv) { }
	
	public void OnXLineAdd(server serv, char type, final String value) { }
	public void OnXLineDel(server serv, char type, final String value) { }
	
	public void onServerLink(server serv, server to) { }
	public void onServerSplit(server serv, server from) { }
	
	private static LinkedList<event> events = new LinkedList<event>();
	
	public static final event[] getEvents()
	{
		event[] a = new event[events.size()];
		events.toArray(a);
		return a;
	}
}