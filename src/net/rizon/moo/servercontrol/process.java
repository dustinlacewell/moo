package net.rizon.moo.servercontrol;

import net.rizon.moo.moo;

public class process extends Thread
{
	private connection con;
	private String source, target, command;

	public process(connection con, final String source, final String target, final String command)
	{
		this.con = con;
		this.source = source;
		this.target = target;
		this.command = command;
		
		this.con.processes.add(this);
	}
	
	@Override
	public void run()
	{
		try
		{
			if (this.con.isConnected() == false)
				this.con.connect();
			
			this.con.execute(this.command);
			
			for (String in; (in = this.con.readLine()) != null;)
				if (in.trim().isEmpty() == false)
					moo.sock.reply(this.source, this.target, "[" + this.con.getServerInfo().name + ":" + this.con.getProtocol().getProtocolName() + "] " + in);
		}
		catch (Exception ex)
		{
			moo.sock.reply(this.source, this.target, "Error running command on " + this.con.getServerInfo().name + ":" + this.con.getProtocol().getProtocolName() + ": " + ex.getMessage());
			ex.printStackTrace();
		}
		finally
		{
			this.con.processes.remove(this);
		}
	}
}
