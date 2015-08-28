package net.rizon.moo.logging;

import java.lang.Thread.UncaughtExceptionHandler;
import org.slf4j.Logger;

public class LoggerUtils
{
	public static void initThread(final Logger logger, Thread t)
	{
		t.setUncaughtExceptionHandler(new UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				logger.error("uncaught exception", e);
			}
			
		});
	}
}
