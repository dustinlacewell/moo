package net.rizon.moo.servercontrol;

import net.rizon.moo.Logger;

public abstract class FileRemove extends Process
{
	protected String file;

	public FileRemove(Connection con, final String file)
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
			Logger.getGlobalLogger().log(ex);
		}
	}

	public void onError(Exception e) { }
	public void onFinish() { }
}
