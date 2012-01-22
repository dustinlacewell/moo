package net.rizon.moo.servercontrol;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import net.rizon.moo.database;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.servercontrol.protocols.protocolSSH;
import net.rizon.moo.servercontrol.protocols.protocolTelnet;

public class servercontrol extends mpackage
{
	public servercontrol()
	{
		super("Server Control", "Manage servers");
		
		new commandServerControl(this);
		new commandConnections(this);
		
		new protocolSSH();
		new protocolTelnet();
		
		moo.db.executeUpdate("CREATE TABLE IF NOT EXISTS servercontrol (`host` varchar(64), `port` int(11), `protocol` varchar(64), `user` varchar(64), `pass` varchar(64), `group` varchar(64))");
	}
	
	public static final serverInfo[] findServers(final String name, final String protocol)
	{
		try
		{
			PreparedStatement statement = moo.db.prepare("SELECT * FROM servercontrol WHERE (`host` LIKE ? OR `group` = ?) AND `protocol` = ?");
			statement.setString(1, "%" + name + "%");
			statement.setString(2, name);
			statement.setString(3, protocol);
			
			ResultSet rs = moo.db.executeQuery();
			LinkedList<serverInfo> sis = new LinkedList<serverInfo>();
			while (rs.next())
			{
				serverInfo si = new serverInfo();
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
				serverInfo[] sis_array = new serverInfo[sis.size()];
				sis.toArray(sis_array);
				return sis_array;
			}
		}
		catch (SQLException ex)
		{
			database.handleException(ex);
		}
		
		return null;
	}
}