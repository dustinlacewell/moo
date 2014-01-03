package net.rizon.moo.servercontrol;

import java.io.File;

import net.rizon.moo.Moo;

class uploadEchoProcess extends EchoProcess
{
	private File file;
	
	public uploadEchoProcess(Connection con, String source, String target, File file, String command)
	{
		super(con, source, target, command);
		this.file = file;
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();
		
		/* now remove the file */
		new uploadFileRemove(con, file.getName()).run(); 
	}
}

class uploadFileRemove extends FileRemove
{
	public uploadFileRemove(Connection con, String file)
	{
		super(con, file);
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();
		
		for (String ch : Moo.conf.getList("moo_log_channels"))
			Moo.privmsg(ch, "[" + this.con.getServerInfo().name + "] Finished executing " + file);
	}
}

public class UploadProcess extends FileUpload
{
	private String source, target, args;
	
	public UploadProcess(Connection con, File file, final String source, final String target, final String args)
	{
		super(con, file);
		this.source = source;
		this.target = target;
		this.args = args;
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();
		
		/* now for the command! */
		String command = "/bin/sh " + file.getName();
		if (!args.isEmpty())
			command += " " + args;
		
		Process proc = new uploadEchoProcess(con, source, target, file, command);
		proc.run();
	}
}