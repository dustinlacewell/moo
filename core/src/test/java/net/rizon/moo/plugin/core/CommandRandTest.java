package net.rizon.moo.plugin.core;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.inject.Inject;
import net.rizon.moo.CommandSource;
import net.rizon.moo.test.MooJUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/**
 *
 * @author Orillion {@literal <orillion@rizon.net>}
 */
@RunWith(MooJUnitRunner.class)
public class CommandRandTest
{
	private static final String COMMAND_NAME = "!rand";
	
	@Inject
	private CommandRand testCommand;
	
	@Mock
	private CommandSource source;
	
	private final ArgumentCaptor<String> commandSourceCaptor = ArgumentCaptor.forClass(String.class);

	/**
	 * Test of execute method, with no parameters.
	 */
	@Test
	public void testExecuteDefault()
	{
		int expectedLength = 8;
		int actualLength;

		String expectedStringStart = "Rand(" + expectedLength + ") = ";

		String[] params =
		{
			COMMAND_NAME
		};

		testCommand.execute(source, params);
		verify(source, times(1)).reply(commandSourceCaptor.capture());

		String repliedValue = commandSourceCaptor.getValue();
		Assert.assertTrue(repliedValue.startsWith(expectedStringStart));

		// Cut the start of the string (which is correct), so we are left with
		// the actual random String.
		actualLength = repliedValue.substring(expectedStringStart.length()).length();

		assertEquals(expectedLength, actualLength);
	}

	/**
	 * Test of execute method, with length 0.
	 */
	@Test
	public void testExecuteWithLength0()
	{
		int expectedLength = 8;
		int actualLength;

		String expectedStringStart = "Rand(" + expectedLength + ") = ";

		String[] params =
		{
			COMMAND_NAME,
			"0"
		};

		testCommand.execute(source, params);
		verify(source, times(1)).reply(commandSourceCaptor.capture());

		String repliedValue = commandSourceCaptor.getValue();
		Assert.assertTrue(repliedValue.startsWith(expectedStringStart));

		// Cut the start of the string (which is correct), so we are left with
		// the actual random String.
		actualLength = repliedValue.substring(expectedStringStart.length()).length();

		assertEquals(expectedLength, actualLength);
	}

	/**
	 * Test of execute method, with length 8.
	 */
	@Test
	public void testExecuteWithLength8()
	{
		int expectedLength = 8;
		int actualLength;

		String expectedStringStart = "Rand(" + expectedLength + ") = ";

		String[] params =
		{
			COMMAND_NAME,
			"8"
		};

		testCommand.execute(source, params);
		verify(source, times(1)).reply(commandSourceCaptor.capture());

		String repliedValue = commandSourceCaptor.getValue();
		Assert.assertTrue(repliedValue.startsWith(expectedStringStart));

		// Cut the start of the string (which is correct), so we are left with
		// the actual random String.
		actualLength = repliedValue.substring(expectedStringStart.length()).length();

		assertEquals(expectedLength, actualLength);
	}

	/**
	 * Test of execute method, with length 9.
	 */
	@Test
	public void testExecuteWithLength9()
	{
		int expectedLength = 9;
		int actualLength;

		String expectedStringStart = "Rand(" + expectedLength + ") = ";

		String[] params =
		{
			COMMAND_NAME,
			"9"
		};

		testCommand.execute(source, params);
		verify(source, times(1)).reply(commandSourceCaptor.capture());

		String repliedValue = commandSourceCaptor.getValue();
		Assert.assertTrue(repliedValue.startsWith(expectedStringStart));

		// Cut the start of the string (which is correct), so we are left with
		// the actual random String.
		actualLength = repliedValue.substring(expectedStringStart.length()).length();

		assertEquals(expectedLength, actualLength);
	}

	/**
	 * Test of execute method, with length 300.
	 */
	@Test
	public void testExecuteWithLength300()
	{
		int expectedLength = 300;
		int actualLength;

		String expectedStringStart = "Rand(" + expectedLength + ") = ";

		String[] params =
		{
			COMMAND_NAME,
			"300"
		};

		testCommand.execute(source, params);
		verify(source, times(1)).reply(commandSourceCaptor.capture());

		String repliedValue = commandSourceCaptor.getValue();
		Assert.assertTrue(repliedValue.startsWith(expectedStringStart));

		// Cut the start of the string (which is correct), so we are left with
		// the actual random String.
		actualLength = repliedValue.substring(expectedStringStart.length()).length();

		assertEquals(expectedLength, actualLength);
	}

	/**
	 * Test of execute method, with length 301.
	 */
	@Test
	public void testExecuteWithLength301()
	{
		int expectedLength = 300;
		int actualLength;

		String expectedStringStart = "Rand(" + expectedLength + ") = ";

		String[] params =
		{
			COMMAND_NAME,
			"301"
		};

		testCommand.execute(source, params);
		verify(source, times(1)).reply(commandSourceCaptor.capture());

		String repliedValue = commandSourceCaptor.getValue();
		Assert.assertTrue(repliedValue.startsWith(expectedStringStart));

		// Cut the start of the string (which is correct), so we are left with
		// the actual random String.
		actualLength = repliedValue.substring(expectedStringStart.length()).length();

		assertEquals(expectedLength, actualLength);
	}

	// @TODO: Implement tests for cases where params.length > 1.
	// @TODO: Implement tests for cases where params[1] does not equal an Integer.
}
