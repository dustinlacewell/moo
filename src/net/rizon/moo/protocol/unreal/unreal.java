package net.rizon.moo.protocol.unreal;

import net.rizon.moo.MPackage;
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

public class unreal extends MPackage
{
	public unreal()
	{
		super("Unreal", "UnrealIRCd protocol functions");
		
		/* Core */
		new Message001();
		new Message213();
		new Message219();
		new Message243();
		new Message364();
		new Message365();
		new Message474();
		new MessageInvite();
		new MessagePing();
		new MessagePrivmsg();
		new MessageJoin();
		new MessagePart();
		new MessageKick();
		new MessageMode();
		new MessageNick();
		new MessageNotice();
		new MessageQuit();
		new MessageWallops();
		
		/* Unreal */
		new Message006();
		new Message007();
	}
}