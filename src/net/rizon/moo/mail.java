package net.rizon.moo;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

class outputWriter extends OutputStreamWriter
{
	public outputWriter(OutputStream out)
	{
		super(out);
	}
	
	public void write(final String str) throws IOException
	{
		this.write(str + "\n", 0, str.length() + 1);
	}
}

class mailThread extends Thread
{
	private String path;
	private String to;
	private String subject;
	private String message;
	
	public mailThread(final String path, final String to, final String subject, final String message)
	{
		this.path = path;
		this.to = to;
		this.subject = subject;
		this.message = message;
	}
	
	@Override
	public void run()
	{
		try
		{
			Process proc = Runtime.getRuntime().exec(this.path + " -t");
			outputWriter out = new outputWriter(proc.getOutputStream());
			
			out.write("From: moo@rizon.net");
			out.write("To: " + this.to);
			out.write("Subject: " + this.subject);
			out.write(this.message);
			out.write(".");
			
			out.close();
			proc.getInputStream().close();
			proc.getErrorStream().close();
			
			if (moo.conf.getDebug() > 0)
				System.out.println("Successfully sent message");
		}
		catch (IOException ex)
		{
			ex.printStackTrace();
		}
	}
}

public class mail
{
	public static void send(final String to, final String subject, final String message)
	{
		if (moo.conf.getSendmailPath().isEmpty() == true)
			return;
		
		File sendmail = new File(moo.conf.getSendmailPath());
		if (sendmail.exists() == false || sendmail.isFile() == false)
			return;
		
		mailThread t = new mailThread(moo.conf.getSendmailPath(), to, subject, message);
		t.start();
	}
}
