package net.rizon.moo.plugin.proxyscan;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProxyStats extends Command
{
	private static final Logger logger = LoggerFactory.getLogger(ProxyStats.class);
	
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
			PreparedStatement ps = Moo.db.prepare("select protocol,port,count(*) from proxies group by protocol, port order by count(*) desc");
			ResultSet rs = Moo.db.executeQuery(ps);
			while (rs.next())
				source.reply(rs.getString("protocol") + " " + rs.getString("port") + ": " + rs.getString("count(*)"));
			rs.close();
			ps.close();
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to get proxyscan stats", ex);
		}
	}
}
