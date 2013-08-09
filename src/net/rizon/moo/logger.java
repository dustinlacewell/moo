package net.rizon.moo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

final class loggerHandler extends Handler
{
	private static final DateFormat format = new SimpleDateFormat("E M kk:mm:ss:SSSS yyyy");

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

		if (moo.conf != null && moo.sock != null)
			if (record.getLevel() == Level.INFO || bad)
				for (final String ch : moo.conf.getMooLogChannels())
				{
					if (message != null)
						moo.privmsg(ch, message);
					if (stes != null)
					{
						moo.privmsg(ch, record.getThrown().toString());
						for (StackTraceElement ste : stes)
							moo.privmsg(ch, ste.toString());
					}
				}

		if ((moo.conf != null && moo.conf.getDebug() > 0) || bad)
		{
			System.out.println(format.format(new Date(record.getMillis())) + " [" + record.getLevel().getName() + "] [" + record.getLoggerName() + "] " + message);
			if (stes != null)
			{
				System.out.println(record.getThrown().toString());
				for (StackTraceElement ste : stes)
					System.out.println(ste.toString());
			}
		}
	}
}

public class logger extends Logger
{
	private static HashMap<String, logger> loggers = new HashMap<String, logger>();
	private static final loggerHandler handler = new loggerHandler();
	
	protected logger(String name, String resourceBundleName)
	{
		super(name, resourceBundleName);
	}

	public static logger getLogger(final String name)
	{
		logger l = loggers.get(name);
		if (l != null)
			return l;
	
		l = new logger(name, null);
		loggers.put(name, l);
		
		l.setLevel(Level.ALL);
		l.addHandler(handler);
		return l;
	}
	
	public static logger getGlobalLogger()
	{
		return getLogger(Logger.GLOBAL_LOGGER_NAME);
	}
	
	public void log(Exception ex)
	{
		this.log(Level.SEVERE, null, ex);
	}
}