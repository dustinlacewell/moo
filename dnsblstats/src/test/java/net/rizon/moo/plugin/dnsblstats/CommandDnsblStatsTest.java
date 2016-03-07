package net.rizon.moo.plugin.dnsblstats;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import java.util.List;
import net.rizon.moo.CommandSource;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Orillion {@literal <orillion@rizon.net>}
 */
@RunWith(MockitoJUnitRunner.class)
public class CommandDnsblStatsTest
{
	private static final String COMMAND_NAME = "!dnsblstats";
	private static final String COMMAND_ARGUMENT = "server";
	private static final String SERVER_NAME = "test.rizon.net";

	// Empty list of required channels, we don't use that anyway in this test.
	private static final String[] REQUIRED_CHANNELS =
	{
	};

	@Inject
	private CommandDnsblStats testCommand;

	@Bind
	@Mock
	private Protocol mockProtocol;

	@Bind
	@Mock
	private ServerManager mockServerManager;

	@Mock
	private Server mockServer;

	@Mock
	private CommandSource mockCommandSource;

	@Bind
	@Mock
	private Config mockConfig;

	private final ArgumentCaptor<String> stringCaptor = ArgumentCaptor.forClass(String.class);

	@Before
	public void setUp()
	{
		mockConfig.admin_channels = REQUIRED_CHANNELS;
		mockConfig.oper_channels = REQUIRED_CHANNELS;
		mockConfig.staff_channels = REQUIRED_CHANNELS;

		Guice.createInjector(BoundFieldModule.of(this)).injectMembers(this);
	}

	/**
	 * Test of execute method, of class CommandDnsblStats.
	 */
	@Test
	public void testExecute()
	{
		String expectedCommand = "STATS";
		String expectedArgument = "B";
		String expectedServer = SERVER_NAME;

		String[] params =
		{
			COMMAND_NAME
		};

		Server[] servers =
		{
			mockServer
		};

		// Return one fake server when requesting all servers.
		when(mockServerManager.getServers()).thenReturn(servers);

		when(mockServer.isNormal()).thenReturn(true);
		when(mockServer.isHub()).thenReturn(false);
		when(mockServer.getName()).thenReturn(SERVER_NAME);

		testCommand.execute(mockCommandSource, params);

		verify(mockProtocol, times(1)).write(stringCaptor.capture(), stringCaptor.capture(), stringCaptor.capture());
		List<String> capturedStrings = stringCaptor.getAllValues();

		// "STATS"
		assertEquals(expectedCommand, capturedStrings.get(0));
		// "B"
		assertEquals(expectedArgument, capturedStrings.get(1));
		// "test.rizon.net"
		assertEquals(expectedServer, capturedStrings.get(2));
	}
}
