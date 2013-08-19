package net.rizon.moo.servercontrol;

import java.io.File;

import net.rizon.moo.moo;

class uploadEchoProcess extends echoProcess
{
	private File file;
	
	public uploadEchoProcess(connection con, String source, String target, File file, String command)
	{
		super(con, source, target, command);
		this.file = file;
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();
		
		/* now remove the file */
		new uploadFileRemove(con, file.getName()).start(); 
	}
}

class uploadFileRemove extends fileRemove
{
	public uploadFileRemove(connection con, String file)
	{
		super(con, file);
	}
	
	@Override
	public void onFinish()
	{
		super.onFinish();
		
		for (String ch : moo.conf.getMooLogChannels())
			moo.privmsg(ch, "[" + this.con.getServerInfo().name + "] Finished executing " + file);
	}
}

public class uploadProcess extends fileUpload
{
	private String source, target, args;
	
	public uploadProcess(connection con, File file, final String source, final String target, final String args)
	{
		super(con, file);
		this.source = source;
		this.target = target;
		this.args = args;
	}
	
	@Override
	public void onFinish()
	{
		for (String ch : moo.conf.getMooLogChannels())
			moo.privmsg(ch, "[" + this.con.getServerInfo().name + "] Successfully uploaded " + file.getName());
		
		/* now for the command! */
		String command = "/bin/sh " + file.getName();
		if (!args.isEmpty())
			command += " " + args;
		
		process proc = new uploadEchoProcess(con, source, target, file, command);
		proc.start();
	}
}