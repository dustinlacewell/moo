package net.rizon.moo.plugin.dnsblstats;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import net.rizon.moo.CommandSource;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.irc.Server;
import net.rizon.moo.irc.ServerManager;
import net.rizon.moo.test.MooJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

/**
 *
 * @author Orillion {@literal <orillion@rizon.net>}
 */
@RunWith(MooJUnitRunner.class)
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
