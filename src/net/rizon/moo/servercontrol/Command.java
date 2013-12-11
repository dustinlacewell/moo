package net.rizon.moo.servercontrol;

import net.rizon.moo.Logger;

public abstract class Command extends Process
{
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
