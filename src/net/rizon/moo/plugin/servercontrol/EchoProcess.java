package net.rizon.moo.plugin.servercontrol;

import net.rizon.moo.Moo;

public class EchoProcess extends Command
{
	private String source, target;
	
	public EchoProcess(Connection con, final String source, final String target, final String command)
	{
		super(con, command);
		this.source = source;
		this.target = target;
	}

	@Override
	public void onLine(String in)
	{
		Moo.reply(this.source, this.target, "[" + this.con.getServerInfo().name + ":" + this.con.getProtocol().getProtocolName() + "] " + in);
	}
}