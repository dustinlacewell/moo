package net.rizon.moo.plugin.commands;

import net.rizon.moo.plugin.commands.uptime.CommandUptime;
import net.rizon.moo.plugin.commands.why.CommandWhy;
import net.rizon.moo.plugin.commands.version.CommandVersions;
import com.google.inject.multibindings.Multibinder;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Message;
import net.rizon.moo.Plugin;
import net.rizon.moo.plugin.commands.map.CommandMapAll;
import net.rizon.moo.plugin.commands.map.CommandMapRegular;
import net.rizon.moo.plugin.commands.map.Message211;
import net.rizon.moo.plugin.commands.map.Message219;
import net.rizon.moo.plugin.commands.map.Message265;
import net.rizon.moo.plugin.commands.sid.CommandSidClient;
import net.rizon.moo.plugin.commands.sid.CommandSidHub;
import net.rizon.moo.plugin.commands.uptime.Message242;
import net.rizon.moo.plugin.commands.version.CommandVersion;
import net.rizon.moo.plugin.commands.version.Message351;
import net.rizon.moo.plugin.commands.why.Message216;
import net.rizon.moo.plugin.commands.why.Message219Why;
import net.rizon.moo.plugin.commands.why.Message225;

public class commands extends Plugin
{
//	private Command climit, oline, slackers, soa, uptime, why;
//	private CommandTime time;
//	private CommandMap map;
//	private CommandSid sid;
//	private CommandVersions version;

	public commands()
	{
		super("Administation Commands", "Common IRC administration commands");
	}

	@Override
	public void start() throws Exception
	{
//		if (Moo.conf.general.protocol == Protocol.PLEXUS)
//		{
//			climit = new CommandClimit(this);
//			map = new CommandMap(this);
//			oline = new CommandOline(this);
//			sid = new CommandSid(this);
//			slackers = new CommandSlackers(this);
//		}
//		soa = new CommandSoa(this);
//		time = new CommandTime(this);
//		uptime = new CommandUptime(this);
//		if (Moo.conf.general.protocol == Protocol.PLEXUS)
//		{
//			version = new CommandVersions(this);
//			why = new CommandWhy(this);
//		}
	}

	@Override
	public void stop()
	{
	}

	@Override
	public List<Command> getCommands()
	{
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	protected void configure()
	{
		bind(commands.class).toInstance(this);

		Multibinder<Command> commandBinder = Multibinder.newSetBinder(binder(), Command.class);
		Multibinder<Message> messageBinder = Multibinder.newSetBinder(binder(), Message.class);

		commandBinder.addBinding().to(CommandClimit.class);
		commandBinder.addBinding().to(CommandOline.class);
		commandBinder.addBinding().to(CommandSlackers.class);
		commandBinder.addBinding().to(CommandSoa.class);
		commandBinder.addBinding().to(CommandTime.class);

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

		commandBinder.addBinding().to(CommandWhy.class);
		messageBinder.addBinding().to(Message216.class);
		messageBinder.addBinding().to(Message219Why.class);
		messageBinder.addBinding().to(Message225.class);

		commandBinder.addBinding().to(CommandUptime.class);
		messageBinder.addBinding().to(Message242.class);
	}
}
