package net.rizon.moo.servercontrol;

import net.rizon.moo.moo;

public class process extends Thread
{
	private connection con;
	private String source, target;

	public process(connection con, final String source, final String target)
	{
		this.con = con;
		this.source = source;
		this.target = target;
		
		this.con.processes.add(this);
	}
	
	@Override
	public void run()
	{
		for (String in; (in = this.con.readLine()) != null;)
			moo.sock.reply(this.source, this.target, "[" + this.con.getHost() + "] " + in);
		
		this.con.processes.remove(this);
	}
}
