package net.rizon.moo.test;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Orillion {@literal <orillion@rizon.net>}
 */
public class MooJUnitRunner extends BlockJUnit4ClassRunner
{
	public MooJUnitRunner(Class<?> klass) throws InitializationError
	{
		super(klass);
		// Perform stuff on static variables here
	}

	@Override
	public Object createTest() throws Exception
	{
		// Create a new test object
		Object testObject = super.createTest();

		// Perform stuff on member variables here
		// Init mocks first, else we're binding null objects
		MockitoAnnotations.initMocks(testObject);
		// Inject everything in the test object
		Guice.createInjector(BoundFieldModule.of(testObject)).injectMembers(testObject);

		return testObject;
	}

	@Override
	public void run(final RunNotifier notifier)
	{
		notifier.addListener(new RunListener()
		{
			@Override
			public void testFinished(Description description) throws Exception
			{
				super.testFinished(description);
				
				try
				{
					Mockito.validateMockitoUsage();
				}
				catch (Throwable t)
				{
					notifier.fireTestFailure(new Failure(description, t));
				}
			}
		});

		super.run(notifier);
	}
}
