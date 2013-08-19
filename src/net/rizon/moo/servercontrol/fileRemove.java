package net.rizon.moo.servercontrol;

import net.rizon.moo.logger;

public abstract class fileRemove extends process
{
	protected String file;

	public fileRemove(connection con, final String file)
	{
		super(con);
		this.file = file;
	}
	
	@Override
	public void onRun()
	{
		try
		{
			if (this.con.isConnected() == false)
				this.con.connect();

			this.con.remove(file);
			
			this.onFinish();
		}
		catch (Exception ex)
		{
			this.onError(ex);
			logger.getGlobalLogger().log(ex);
		}
	}

	public void onError(Exception e) { }
	public void onFinish() { }
}
