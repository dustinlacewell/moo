package net.rizon.moo.protocol.plexus;

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
import net.rizon.moo.protocol.messageNotice;
import net.rizon.moo.protocol.messagePart;
import net.rizon.moo.protocol.messagePing;
import net.rizon.moo.protocol.messagePrivmsg;
import net.rizon.moo.protocol.messageQuit;
import net.rizon.moo.protocol.messageWallops;

public class plexus extends mpackage
{
	public plexus()
	{
		super("Plexus", "Plexus protocol functions");
		
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
		new messageNotice();
		new messageQuit();
		new messageWallops();
		
		/* Plexus */
		new message015();
		new message017();
		new message227();
		
		new eventPlexus();
	}
}