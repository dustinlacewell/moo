package net.rizon.moo.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import net.rizon.moo.Moo;

public class ConsoleFilter extends Filter<ILoggingEvent>
{
	@Override
	public FilterReply decide(ILoggingEvent event)
	{
		boolean show = Moo.conf.debug || event.getLevel() != Level.DEBUG;
		return show ? FilterReply.ACCEPT : FilterReply.DENY;
	}

}
