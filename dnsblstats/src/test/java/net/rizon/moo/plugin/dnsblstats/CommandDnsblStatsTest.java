package net.rizon.moo.plugin.dnsblstats;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import com.google.inject.testing.fieldbinder.BoundFieldModule;
import net.rizon.moo.CommandSource;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
	private static final String SERVER_NAME = "test.rizon.net";

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

	@Before
	public void setUp()
	{
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

		verify(mockProtocol, times(1)).write(
				expectedCommand, expectedArgument, expectedServer);
	}
}
