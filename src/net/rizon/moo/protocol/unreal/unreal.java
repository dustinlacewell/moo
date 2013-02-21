package net.rizon.moo.protocol.unreal;

import net.rizon.moo.mpackage;
import net.rizon.moo.protocol.message001;
import net.rizon.moo.protocol.message213;
import net.rizon.moo.protocol.message219;
import net.rizon.moo.protocol.message243;
import net.rizon.moo.protocol.message364;
import net.rizon.moo.protocol.message365;
import net.rizon.moo.protocol.message474;
import net.rizon.moo.protocol.messageInvite;
import net.rizon.moo.protocol.messageJoin;
import net.rizon.moo.protocol.messageKick;
import net.rizon.moo.protocol.messageMode;
import net.rizon.moo.protocol.messageNick;
import net.rizon.moo.protocol.messagePart;
import net.rizon.moo.protocol.messagePing;
import net.rizon.moo.protocol.messagePrivmsg;
import net.rizon.moo.protocol.messageQuit;

public class unreal extends mpackage
{
	public unreal()
	{
		super("Unreal", "UnrealIRCd protocol functions");
		
		/* Core */
		new message001();
		new message213();
		new message219();
		new message243();
		new message364();
		new message365();
		new message474();
		new messageInvite();
		new messagePing();
		new messagePrivmsg();
		new messageJoin();
		new messagePart();
		new messageKick();
		new messageMode();
		new messageNick();
		new messageQuit();
		
		/* Unreal */
		new message006();
		new message007();
	}
}