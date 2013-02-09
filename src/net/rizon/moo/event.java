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
	public void OnOLineChange(final server serv, final String oper, final String diff) { }
	public void OnXLineDel(server serv, char type, final String value) { }
	
	public void onServerLink(server serv, server to) { }
	public void onServerSplit(server serv, server from) { }
	
	public void onJoin(final String source, final String channel) { }
	public void onPart(final String source, final String channel) { }
	public void onKick(final String source, final String target, final String channel) { }
	public void onMode(final String source, final String channel, final String modes) { }
	public void onPrivmsg(final String source, final String channel, final String message) { }
	public void onNick(final String source, final String dest) { }
	public void onQuit(final String source, final String reason) { }
	
	private static LinkedList<event> events = new LinkedList<event>();
	
	public static final event[] getEvents()
	{
		event[] a = new event[events.size()];
		events.toArray(a);
		return a;
	}
}