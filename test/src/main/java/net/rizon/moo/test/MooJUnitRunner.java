package net.rizon.moo.test;

import com.google.inject.Guice;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;
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
}
