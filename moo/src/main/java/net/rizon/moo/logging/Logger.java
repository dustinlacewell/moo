package net.rizon.moo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import java.util.ArrayList;
import java.util.List;
import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;

public class Logger extends UnsynchronizedAppenderBase<ILoggingEvent>
{
	@Override
	protected void append(ILoggingEvent event)
	{
		if (Moo.injector == null)
		{
			return;
		}

		Level level = event.getLevel();
		if (!level.isGreaterOrEqual(Level.INFO))
		{
			return;
		}

		String message = event.getFormattedMessage();
		IThrowableProxy throwable = event.getThrowableProxy();
		StackTraceElement[] stes = null;

		if (throwable != null)
		{
			List<StackTraceElement> list = new ArrayList<>();
			for (StackTraceElementProxy step : throwable.getStackTraceElementProxyArray())
			{
				list.add(step.getStackTraceElement());
			}
			stes = list.toArray(new StackTraceElement[0]);
		}

		Config conf = Moo.injector.getInstance(Config.class);
		Protocol protocol = Moo.injector.getInstance(Protocol.class);

		for (final String ch : conf.moo_log_channels)
		{
			if (message != null)
			{
				protocol.privmsg(ch, message);
			}
			if (stes != null)
			{
				if (throwable != null)
				{
					String exception = throwable.getMessage() == null ? throwable.getClassName() : throwable.getMessage();

					if (exception != null)
					{
						protocol.privmsg(ch, exception);
					}
				}

				for (StackTraceElement ste : stes)
				{
					protocol.privmsg(ch, ste.toString());
				}
			}
		}
	}
}
