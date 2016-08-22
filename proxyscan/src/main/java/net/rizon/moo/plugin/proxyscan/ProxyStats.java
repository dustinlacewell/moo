package net.rizon.moo.plugin.proxyscan;

import com.google.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import org.slf4j.Logger;

public class ProxyStats extends Command
{
	@Inject
	private static Logger logger;

	@Inject
	public ProxyStats(Config conf)
	{
		super("!PROXYSTATS", "View proxy hit statistics");
		this.requiresChannel(conf.oper_channels);
		this.requiresChannel(conf.admin_channels);
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		source.reply("Proxy stats:");
		try
		{
			try (PreparedStatement ps = Moo.db.prepare("select protocol,port,count(*) from proxies group by protocol, port order by count(*) desc"))
			{
				try (ResultSet rs = Moo.db.executeQuery(ps))
				{
					while (rs.next())
						source.reply(rs.getString("protocol") + " " + rs.getString("port") + ": " + rs.getString("count(*)"));
					rs.close();
				}
			}
		}
		catch (SQLException ex)
		{
			logger.warn("Unable to get proxyscan stats", ex);
		}
	}
}
