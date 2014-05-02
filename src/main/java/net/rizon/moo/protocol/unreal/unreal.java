package net.rizon.moo.protocol.unreal;

import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.protocol.Message001;
import net.rizon.moo.protocol.Message213;
import net.rizon.moo.protocol.Message219;
import net.rizon.moo.protocol.Message243;
import net.rizon.moo.protocol.Message364;
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

public class unreal extends Plugin
{
	private Message m001, m213, m219, m243, m364, m474, invite, ping, privmsg, join, part,
					kick, mode, nick, notice, quit, wallops,
					m006, m007;

	public unreal()
	{
		super("Unreal", "UnrealIRCd protocol functions");
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
		
		/* Unreal */
		m006 = new Message006();
		m007 = new Message007();
	}

	@Override
	public void stop()
	{
		m001.remove();
		m213.remove();
		m219.remove();
		m243.remove();
		m364.remove();
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
		
		m006.remove();
		m007.remove();
	}
}