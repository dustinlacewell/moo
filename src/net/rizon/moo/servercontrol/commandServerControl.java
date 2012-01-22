package net.rizon.moo.servercontrol;

import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;

public class commandServerControl extends command
{
	public commandServerControl(mpackage pkg)
	{
		super(pkg, "!EXEC", "Execute commands on remote servers");
		this.requireAdmin();
	}
	
	@Override
	public void execute(String source, String target, String[] params)
	{
		if (params.length < 4)
		{
			moo.sock.reply(source, target, "Syntax: !exec server protocol command");
			return;
		}
		
		protocol proto = protocol.findProtocol(params[2]);
		if (proto == null)
		{
			moo.sock.reply(source, target, "No such protocol " + params[2]);
			return;
		}
		
		serverInfo[] server_info = servercontrol.findServers(params[1]);
		if (server_info == null)
		{
			moo.sock.reply(source, target, "No servers found for " + params[1]);
			return;
		}
		
		String command = params[3];
		for (int i = 4; i < params.length; ++i)
			command += " " + params[i];
		
		for (serverInfo si : server_info)
		{
			try
			{
				connection con = connection.findProcess(si.host, si.protocol);
				if (con == null)
				{
					con = proto.createConnection();
					con.setHost(si.host);
					if (si.port > 0)
						con.setPort(si.port);
					con.setUser(si.user);
					con.setPassword(si.pass);
					
					con.connect();
				}
			
				con.execute(command);
				process proc = new process(con, source, target);
				proc.start();
			}
			catch (Exception ex)
			{
				moo.sock.reply(source, target, "Error executing command on " + si.host + ": " + ex.getMessage());
			}
		}
	}
}
