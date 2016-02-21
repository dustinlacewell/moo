package net.rizon.moo.plugin.commands;

import com.google.inject.Inject;
import net.rizon.moo.plugin.commands.why.CommandWhy;
import net.rizon.moo.plugin.commands.version.CommandVersions;
import com.google.inject.multibindings.Multibinder;
import java.util.Arrays;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.commands.climit.CommandClimit;
import net.rizon.moo.plugin.commands.climit.Message005;
import net.rizon.moo.plugin.commands.climit.Message105;
import net.rizon.moo.plugin.commands.map.CommandMapAll;
import net.rizon.moo.plugin.commands.map.CommandMapRegular;
import net.rizon.moo.plugin.commands.map.Message211;
import net.rizon.moo.plugin.commands.map.Message219;
import net.rizon.moo.plugin.commands.map.Message265;
import net.rizon.moo.plugin.commands.sid.CommandSidClient;
import net.rizon.moo.plugin.commands.sid.CommandSidHub;
import net.rizon.moo.plugin.commands.slackers.CommandSlackers;
import net.rizon.moo.plugin.commands.slackers.Message219Slackers;
import net.rizon.moo.plugin.commands.slackers.Message249;
import net.rizon.moo.plugin.commands.time.CommandTime;
import net.rizon.moo.plugin.commands.time.Message391;
import net.rizon.moo.plugin.commands.uptime.CommandUptime;
import net.rizon.moo.plugin.commands.uptime.Message242;
import net.rizon.moo.plugin.commands.version.CommandVersion;
import net.rizon.moo.plugin.commands.version.Message351;
import net.rizon.moo.plugin.commands.why.Message216;
import net.rizon.moo.plugin.commands.why.Message219Why;
import net.rizon.moo.plugin.commands.why.Message225;

public class commands extends Plugin
{
	@Inject
	private CommandOline oline;

	@Inject
	private CommandSoa soa;

	@Inject
	private CommandClimit climit;

	@Inject
	private CommandMapAll mapAll;
	@Inject
	private CommandMapRegular mapRegular;

	@Inject
	private CommandSidClient sidClient;
	@Inject
	private CommandSidHub sidHub;

	@Inject
	private CommandTime time;

	@Inject
	private CommandUptime uptime;

	@Inject
	private CommandVersion version;
	@Inject
	private CommandVersions versions;

	@Inject
	private CommandWhy why;
	
	public commands()
	{
		super("Administation Commands", "Common IRC administration commands");
	}

	@Override
	public void start() throws Exception
	{
	}

	@Override
	public void stop()
	{
	}

	@Override
	public List<Command> getCommands()
	{
		return Arrays.asList(oline, soa, climit, mapAll, mapRegular, sidClient, sidHub, time, uptime, version, versions, why);
	}

	@Override
	protected void configure()
	{
		bind(commands.class).toInstance(this);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		Multibinder<Message> messageBinder = Multibinder.newSetBinder(binder(), Message.class);

		commandBinder.addBinding().to(CommandOline.class);
		commandBinder.addBinding().to(CommandSoa.class);

		commandBinder.addBinding().to(CommandClimit.class);
		messageBinder.addBinding().to(Message005.class);
		messageBinder.addBinding().to(Message105.class);

		commandBinder.addBinding().to(CommandMapAll.class);
		commandBinder.addBinding().to(CommandMapRegular.class);
		messageBinder.addBinding().to(Message211.class);
		messageBinder.addBinding().to(Message219.class);
		messageBinder.addBinding().to(Message265.class);

		commandBinder.addBinding().to(CommandVersion.class);
		commandBinder.addBinding().to(CommandVersions.class);
		messageBinder.addBinding().to(Message351.class);

		commandBinder.addBinding().to(CommandSidClient.class);
		commandBinder.addBinding().to(CommandSidHub.class);

		commandBinder.addBinding().to(CommandSlackers.class);
		messageBinder.addBinding().to(Message219Slackers.class);
		messageBinder.addBinding().to(Message249.class);

		commandBinder.addBinding().to(CommandTime.class);
		messageBinder.addBinding().to(Message391.class);

		commandBinder.addBinding().to(CommandWhy.class);
		messageBinder.addBinding().to(Message216.class);
		messageBinder.addBinding().to(Message219Why.class);
		messageBinder.addBinding().to(Message225.class);

		commandBinder.addBinding().to(CommandUptime.class);
		messageBinder.addBinding().to(Message242.class);
	}
}
