package net.rizon.moo.plugin.servercontrol;

import java.util.logging.Level;

import net.rizon.moo.Logger;

public abstract class Command extends Process
{
	private static final Logger log = Logger.getLogger(Command.class.getName());
	
	private String command;

	public Command(Connection con, final String command)
	{
		super(con);
		this.command = command;
	}
	
	@Override
	public void onRun()
	{
		try
		{
			if (this.con.isConnected() == false)
				this.con.connect();
			
			log.log(Level.FINE, "Command: " + command);
				
			this.con.execute(this.command);
			
			for (String in; (in = this.con.readLine()) != null;)
				if (in.trim().isEmpty() == false)
					this.onLine(in);

			this.onFinish();
		}
		catch (Exception ex)
		{
			this.onError(ex);
			Logger.getGlobalLogger().log(ex);
		}
	}
	
	public abstract void onLine(String in);
	public void onError(Exception e) { }
	public void onFinish() { }
}
