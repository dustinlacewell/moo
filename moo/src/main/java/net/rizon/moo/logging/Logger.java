package net.rizon.moo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import java.util.ArrayList;
import java.util.List;
import net.rizon.moo.Moo;

public class Logger extends UnsynchronizedAppenderBase<ILoggingEvent>
{
	@Override
	protected void append(ILoggingEvent event)
	{		
		String message = event.getMessage();
		IThrowableProxy throwable = event.getThrowableProxy();
		StackTraceElement[] stes = null;

		if (throwable != null)
		{
			List<StackTraceElement> list = new ArrayList<>();
			for (StackTraceElementProxy step : throwable.getStackTraceElementProxyArray())
				list.add(step.getStackTraceElement());
			stes = list.toArray(new StackTraceElement[0]);
		}

		Level level = event.getLevel();
		boolean bad = level == Level.ERROR || level == Level.WARN || level == Level.ALL;

		if (Moo.conf != null)
			if (level == Level.INFO || bad)
				for (final String ch : Moo.conf.moo_log_channels)
				{
//					if (message != null)
//						Moo.privmsg(ch, message); // XXX logback makes this so can't really inject? post event? cant get eventbus.
//					if (stes != null)
//					{
//						if (throwable != null)
//							Moo.privmsg(ch, throwable.getMessage());
//						for (StackTraceElement ste : stes)
//							Moo.privmsg(ch, ste.toString());
//					}
				}
	}
}
