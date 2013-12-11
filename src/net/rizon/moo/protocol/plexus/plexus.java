package net.rizon.moo.protocol.plexus;

import net.rizon.moo.Event;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.protocol.Message001;
import net.rizon.moo.protocol.Message213;
import net.rizon.moo.protocol.Message219;
import net.rizon.moo.protocol.Message243;
import net.rizon.moo.protocol.Message364;
import net.rizon.moo.protocol.Message365;
import net.rizon.moo.protocol.Message474;
import net.rizon.moo.protocol.MessageInvite;
import net.rizon.moo.protocol.MessageJoin;
import net.rizon.moo.protocol.MessageKick;
import net.rizon.moo.protocol.MessageMode;
import net.rizon.moo.protocol.MessageNick;
import net.rizon.moo.protocol.MessageNotice;
import net.rizon.moo.protocol.MessagePart;
import net.rizon.moo.protocol.MessagePing;
import net.rizon.moo.protocol.MessagePrivmsg;
import net.rizon.moo.protocol.MessageQuit;
import net.rizon.moo.protocol.MessageWallops;

public class plexus extends Plugin
{
	private Message m001, m213, m219, m243, m364, m365, m474, invite, ping, privmsg, join, part,
					kick, mode, nick, notice, quit, wallops,
					m015, m017, m227;
	
	private Event e;
	
	public plexus()
	{
		super("Plexus", "Plexus protocol functions");
	}

	@Override
	public void start() throws Exception
	{
		/* Core */
		m001 = new Message001();
		m213 = new Message213();
		m219 = new Message219();
		m243 = new Message243();
		m364 = new Message364();
		m365 = new Message365();
		m474 = new Message474();
		invite = new MessageInvite();
		ping = new MessagePing();
		privmsg = new MessagePrivmsg();
		join = new MessageJoin();
		part = new MessagePart();
		kick = new MessageKick();
		mode = new MessageMode();
		nick = new MessageNick();
		notice = new MessageNotice();
		quit = new MessageQuit();
		wallops = new MessageWallops();
		
		/* Plexus */
		m015 = new Message015();
		m017 = new Message017();
		m227 = new Message227();
		
		e = new EventPlexus();
	}

	@Override
	public void stop()
	{
		m001.remove();
		m213.remove();
		m219.remove();
		m243.remove();
		m364.remove();
		m365.remove();
		m474.remove();
		invite.remove();
		ping.remove();
		privmsg.remove();
		join.remove();
		part.remove();
		kick.remove();
		mode.remove();
		nick.remove();
		notice.remove();
		quit.remove();
		wallops.remove();
		
		m015.remove();
		m017.remove();
		m227.remove();
		
		e.remove();
	}
}