package net.rizon.moo;

import com.google.inject.Inject;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.FutureListener;
import org.slf4j.Logger;

/**
 * Reports any errors when a future operation is completed and is not completed
 * successfully or canceled.
 * <p>
 * @author Orillion {@literal <orillion@rizon.net>}
 */
public class FutureExceptionListener<T> implements FutureListener<T>
{
	@Inject
	private static Logger logger;

	@Override
	public void operationComplete(Future<T> future) throws Exception
	{
		if (future.isSuccess() || future.isCancelled())
		{
			return;
		}

		Throwable t = future.cause();

		logger.error("Scheduled operation failed: " + t.toString(), t);
	}

}
