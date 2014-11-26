package net.rizon.moo.plugin.proxyscan;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;

public class ProxyStats extends Command
{
	public ProxyStats(Plugin pkg)
	{
		super(pkg, "!PROXYSTATS", "View proxy hit statistics");
		this.requiresChannel(Moo.conf.oper_channels);
		this.requiresChannel(Moo.conf.admin_channels);
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		source.reply("Proxy stats:");
		try
		{
			ResultSet rs = Moo.db.executeQuery("select protocol,port,count(*) from proxies group by protocol, port order by count(*) desc");
			while (rs.next())
				source.reply(rs.getString("protocol") + " " + rs.getString("port") + ": " + rs.getString("count(*)"));
		}
		catch (SQLException ex)
		{
			proxyscan.log.log(Level.WARNING, "Unable to get proxyscan stats", ex);
		}
	}
}
