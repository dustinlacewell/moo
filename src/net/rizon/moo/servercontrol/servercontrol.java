package net.rizon.moo.servercontrol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import net.rizon.moo.Database;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.servercontrol.commands.CommandAddServer;
import net.rizon.moo.servercontrol.commands.CommandConnections;
import net.rizon.moo.servercontrol.commands.CommandDelServer;
import net.rizon.moo.servercontrol.commands.CommandServerControl;
import net.rizon.moo.servercontrol.commands.CommandServers;
import net.rizon.moo.servercontrol.commands.CommandShortcut;
import net.rizon.moo.servercontrol.protocols.FTP;
import net.rizon.moo.servercontrol.protocols.SSH;
import net.rizon.moo.servercontrol.protocols.Telnet;

public class servercontrol extends Plugin
{
	private net.rizon.moo.Command addserver, connections, delserver, sc, servers, s;
	
	public servercontrol()
	{
		super("Server Control", "Manage servers");
		
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS servercontrol (`name` varchar(64) collate nocase, `host` varchar(64), `port` int(11), `protocol` varchar(64) collate nocase, `user` varchar(64), `pass` varchar(64), `group` varchar(64))");
		Moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS shortcuts (`name`, `command`, `file`)");
	}
	
	@Override
	public void start() throws Exception
	{
		addserver = new CommandAddServer(this);
		connections = new CommandConnections(this);
		delserver = new CommandDelServer(this);
		sc = new CommandServerControl(this);
		servers = new CommandServers(this);
		s = new CommandShortcut(this);
		
		new FTP();
		new SSH();
		new Telnet();
	}

	@Override
	public void stop()
	{
		addserver.remove();
		connections.remove();
		delserver.remove();
		sc.remove();
		servers.remove();
		s.remove();
	}
	
	private static final ServerInfo[] processServers(ResultSet rs) throws SQLException
	{
		LinkedList<ServerInfo> sis = new LinkedList<ServerInfo>();
		while (rs.next())
		{
			ServerInfo si = new ServerInfo();
			si.name = rs.getString("name");
			si.host = rs.getString("host");
			si.port = rs.getInt("port");
			si.protocol = rs.getString("protocol");
			si.user = rs.getString("user");
			si.pass = rs.getString("pass");
			si.group = rs.getString("group");
			sis.add(si);
		}
		
		if (sis.size() > 0)
		{
			ServerInfo[] sis_array = new ServerInfo[sis.size()];
			sis.toArray(sis_array);
			return sis_array;
		}
		
		return null;
	}
	
	public static final ServerInfo[] findServers(final String name, final String protocol)
	{
		try
		{
			PreparedStatement statement = Moo.db.prepare("SELECT * FROM servercontrol WHERE (`name` LIKE ? OR `group` = ?) AND `protocol` = ?");
			statement.setString(1, "%" + name + "%");
			statement.setString(2, name);
			statement.setString(3, protocol);
			
			ResultSet rs = Moo.db.executeQuery();
			return processServers(rs);
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
		
		return null;
	}

	public static final ServerInfo[] findServers(final String name)
	{
		try
		{
			PreparedStatement statement = Moo.db.prepare("SELECT * FROM servercontrol WHERE (`name` LIKE ? OR `group` = ?)");
			statement.setString(1, "%" + name + "%");
			statement.setString(2, name);
			
			ResultSet rs = Moo.db.executeQuery();
			return processServers(rs);
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
		
		return null;
	}
	
	public static final ServerInfo[] getServers()
	{
		try
		{
			ResultSet rs = Moo.db.executeQuery("SELECT * FROM servercontrol");
			return processServers(rs);
		}
		catch (SQLException ex)
		{
			Database.handleException(ex);
		}
		
		return null;
	}
}