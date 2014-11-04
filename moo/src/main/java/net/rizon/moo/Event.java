package net.rizon.moo;

import java.util.LinkedList;

public abstract class Event
{
	public Event()
	{
		events.add(this);
	}
	
	public void remove()
	{
		events.remove(this);
	}
	
	public void onConnect() { }
	public void onShutdown() { }
	
	protected void initDatabases() { }
	public void loadDatabases() { }
	public void saveDatabases() { }

	public void onReload(CommandSource source) { }
	
	public void onServerCreate(Server serv) { }
	public void onServerDestroy(Server serv) { }
	
	public void OnXLineAdd(Server serv, char type, final String value) { }
	public void OnOLineChange(final Server serv, final String oper, final String diff) { }
	public void OnXLineDel(Server serv, char type, final String value) { }
	
	public void onServerLink(Server serv, Server to) { }
	public void onServerSplit(Server serv, Server from) { }
	
	public void onJoin(final String source, final String channel) { }
	public void onPart(final String source, final String channel) { }
	public void onKick(final String source, final String target, final String channel) { }
	public void onMode(final String source, final String channel, final String modes) { }
	public void onPrivmsg(final String source, final String channel, final String message) { }
	public void onNotice(final String source, final String channel, final String message) { }
	public void onNick(final String source, final String dest) { }
	public void onQuit(final String source, final String reason) { }
	public void onWallops(final String source, final String message) { }
	
	public void onClientConnect(final String nick, final String ident, final String ip, final String realname) { }
	public void onAkillAdd(final String setter, final String ip, final String reason) { }
	public void onAkillDel(final String setter, final String ip, final String reason) { }
	public void onOPMHit(final String nick, final String ip, final String reason) { }
	public void onDNSBLHit(final String nick, final String ip, final String dnsbl, final String result) { }
	
	private static LinkedList<Event> events = new LinkedList<Event>();
	
	public static final Event[] getEvents()
	{
		Event[] a = new Event[events.size()];
		events.toArray(a);
		return a;
	}
}
