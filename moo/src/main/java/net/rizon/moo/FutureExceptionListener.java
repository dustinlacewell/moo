package net.rizon.moo;

import com.google.inject.Inject;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;

/**
 * Reports any errors when a future operation is completed and is not completed
 * successfully or canceled.
 * <p>
 * @author Orillion {@literal <orillion@rizon.net>}
 */
public class FutureExceptionListener<T> implements FutureListener<T>
{
	public enum ScheduleType
	{
		FIXED_DELAY,
		FIXED_RATE
	}

	@Inject
	private static Logger logger;
	private boolean reschedule = false;
	private Runnable runnable;
	private long delay;
	private long timer;
	private TimeUnit timeUnit;
	private ScheduleType scheduleType;

	@Override
	public void operationComplete(Future<T> future) throws Exception
	{
		if (future.isSuccess() || future.isCancelled())
		{
			return;
		}

		Throwable t = future.cause();

		logger.error("Scheduled operation failed: " + t.toString(), t);

		if (this.reschedule)
		{
			switch (this.scheduleType)
			{
				case FIXED_DELAY:
					Moo.scheduleWithFixedDelay(this.runnable, this.timer, this.timeUnit);
					break;
				case FIXED_RATE:
					Moo.scheduleAtFixedRate(this.runnable, this.timer, this.timeUnit);
					break;
			}
		}
	}

	/**
	 * Sets the parameters in case this future needs to reschedule itself when
	 * it throws an exception.
	 * <p>
	 * @param r     Runnable to run
	 * @param delay Initial delay
	 * @param timer Period between timings
	 * @param unit  TimeUnit
	 * @param type  Type of schedule to use
	 */
	public void setRescheduleParameters(Runnable r, long delay, long timer, TimeUnit unit, ScheduleType type)
	{
		this.reschedule = true;
		this.runnable = r;
		this.delay = delay;
		this.timer = timer;
		this.timeUnit = unit;
		this.scheduleType = type;
	}
}
