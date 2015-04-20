package net.rizon.moo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

final class loggerHandler extends Handler
{
	private static final DateFormat format = new SimpleDateFormat("EEE MMM dd HH:mm:ss:SSSS yyyy");

	public loggerHandler()
	{
		this.setLevel(Level.ALL);
	}

	@Override
	public void close() throws SecurityException { }

	@Override
	public void flush() { }

	@Override
	public void publish(LogRecord record)
	{
		String message = record.getMessage();
		StackTraceElement[] stes = (record.getThrown() != null ? record.getThrown().getStackTrace() : null);
		boolean bad = record.getLevel() == Level.SEVERE || record.getLevel() == Level.WARNING || record.getLevel() == Level.ALL || record.getLevel() == Level.CONFIG;

		if (Moo.conf != null && Moo.sock != null && Moo.sock.isConnected())
			if (record.getLevel() == Level.INFO || bad)
				for (final String ch : Moo.conf.moo_log_channels)
				{
					if (message != null)
						Moo.privmsg(ch, message);
					if (stes != null)
					{
						Moo.privmsg(ch, record.getThrown().toString());
						for (StackTraceElement ste : stes)
							Moo.privmsg(ch, ste.toString());
					}
				}

		if ((Moo.conf != null && Moo.conf.debug) || bad)
		{
			System.out.println(format.format(new Date(record.getMillis())) + " [" + record.getLevel().getName() + "] [" + record.getLoggerName() + "] " + (message != null ? message : ""));
			if (stes != null)
			{
				System.out.println(record.getThrown().toString());
				for (StackTraceElement ste : stes)
					System.out.println(ste.toString());
			}
		}
	}
}

public class Logger extends java.util.logging.Logger implements Thread.UncaughtExceptionHandler
{
	private static HashMap<String, Logger> loggers = new HashMap<String, Logger>();
	private static final loggerHandler handler = new loggerHandler();

	protected Logger(String name, String resourceBundleName)
	{
		super(name, resourceBundleName);
	}

	public static Logger getLogger(final String name)
	{
		Logger l = loggers.get(name);
		if (l != null)
			return l;

		l = new Logger(name, null);
		loggers.put(name, l);

		l.setLevel(Level.ALL);
		l.addHandler(handler);
		return l;
	}

	public static Logger getGlobalLogger()
	{
		return getLogger(java.util.logging.Logger.GLOBAL_LOGGER_NAME);
	}

	public void log(Throwable ex)
	{
		this.log(Level.SEVERE, null, ex);
	}
	
	public void initThread(Thread t)
	{
		t.setUncaughtExceptionHandler(this);
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e)
	{
		log(e);
	}
}
