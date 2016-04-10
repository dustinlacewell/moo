package net.rizon.moo.plugin.vote;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.multibindings.Multibinder;
import java.util.Arrays;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.events.EventListener;
import net.rizon.moo.events.OnReload;
import net.rizon.moo.plugin.vote.conf.Vote;
import net.rizon.moo.plugin.vote.conf.VoteConfiguration;
import org.slf4j.Logger;

public class vote extends Plugin implements EventListener
{
	@Inject
	private static Logger logger;

	private VoteConfiguration conf;

	@Inject
	private CommandVote vote;

	public vote() throws Exception
	{
		super("Vote", "Manages votes");
		conf = VoteConfiguration.load();

		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `votes` (`id` int, `channel` text, `info` text, `owner` text, `date` date, `closed` int)");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS `vote_casts` (`id` int, `channel` text, `voter` text, `vote` text)");
	}

	@Override
	public void start() throws Exception
	{
	}

	@Override
	public void stop()
	{
	}
	
	@Subscribe
	public void onReload(OnReload evt)
	{
		try
		{
			conf = VoteConfiguration.load();
		}
		catch (Exception ex)
		{
			evt.getSource().reply("Error reloading vote configuration: " + ex.getMessage());
			
			logger.warn("Unable to reload configuration", ex);
		}
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.<Command>asList(vote);
	}

	@Override
	protected void configure()
	{
		bind(vote.class).toInstance(this);
		bind(VoteConfiguration.class).toInstance(conf);
		
		Multibinder<EventListener> eventListenerBinder = Multibinder.newSetBinder(binder(), EventListener.class);
		eventListenerBinder.addBinding().toInstance(this);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		commandBinder.addBinding().to(CommandVote.class);
	}
}