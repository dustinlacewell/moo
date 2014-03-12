package net.rizon.moo.servercontrol.commands;

import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.servercontrol.Connection;

public class CommandClose extends Command
{
	public CommandClose(Plugin pkg)
	{
		super(pkg, "!CLOSE", "Close a connection");
		this.requiresChannel(Moo.conf.getList("admin_channels"));
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		int offset = -1;
		
		if (params.length < 2)
		{
			Moo.reply(source, target, "Syntax: !close id");
			return;
		}
		
		try
		{
			offset = Integer.parseInt(params[1]);
		}
		catch (NumberFormatException ex)
		{
			Moo.reply(source, target, "Invalid number " + params[1] + ".");
			return;
		}
		
		if (offset <= 0)
		{
			Moo.reply(source, target, "Connection id less than 1: " + offset + ".");
			return;
		}
		
		Connection[] cons = Connection.getConnections();
		
		if (offset > cons.length)
		{
			Moo.reply(source, target, "Connection id out of range: " + offset + ".");
			return;
		}
		
		/* Subclasses of Connection take care of the protocol-level details to
		 * disconnect properly.
		 */
		cons[offset - 1].destroy();
		
		Moo.reply(source, target, "Closed connection " + offset + ".");
	}
}