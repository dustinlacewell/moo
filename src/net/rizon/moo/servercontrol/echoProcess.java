package net.rizon.moo.servercontrol;

import net.rizon.moo.moo;

public class echoProcess extends command
{
	private String source, target;
	
	public echoProcess(connection con, final String source, final String target, final String command)
	{
		super(con, command);
		this.source = source;
		this.target = target;
	}

	@Override
	public void onLine(String in)
	{
		moo.reply(this.source, this.target, "[" + this.con.getServerInfo().name + ":" + this.con.getProtocol().getProtocolName() + "] " + in);
	}
}