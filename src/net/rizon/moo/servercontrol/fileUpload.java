package net.rizon.moo.servercontrol;

import java.io.File;

import net.rizon.moo.logger;

public abstract class fileUpload extends process
{
	protected File file;

	public fileUpload(connection con, final File file)
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

			this.con.upload(file);
			
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
