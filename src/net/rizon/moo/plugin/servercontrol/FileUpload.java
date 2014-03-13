package net.rizon.moo.plugin.servercontrol;

import java.io.File;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

public abstract class FileUpload extends Process
{
	protected File file;
	protected String dest;

	public FileUpload(Connection con, final File file)
	{
		super(con);
		this.file = file;
		this.dest = file.getName();
	}
	
	public FileUpload(Connection con, final File file, String dest)
	{
		super(con);
		this.file = file;
		this.dest = dest;
	}
	
	@Override
	public void onRun()
	{
		try
		{
			if (this.con.isConnected() == false)
				this.con.connect();

			this.con.upload(file, dest);
			
			this.onFinish();
		}
		catch (Exception ex)
		{
			this.onError(ex);
			Logger.getGlobalLogger().log(ex);
		}
	}

	public void onError(Exception e) { }
	
	public void onFinish()
	{
		for (String ch : Moo.conf.getList("moo_log_channels"))
			Moo.privmsg(ch, "[" + this.con.getServerInfo().name + "] Successfully uploaded " + file.getName());
	}
}
