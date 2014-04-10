package net.rizon.moo.plugin.servercontrol;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.rizon.moo.Event;
import net.rizon.moo.Moo;

public class EventServerControl extends Event
{
	private static final Logger log = Logger.getLogger(EventServerControl.class.getName());
	
	private static Pattern sshRegex = Pattern.compile("^([^ ]*) ssh (?:-p ([0-9]*) )?([^@]*)@([^ ]*) pass: ([^ ]*) .*");
	
	@Override
	public void onPrivmsg(final String source, final String channel, final String message)
	{
		if (!Arrays.asList(Moo.conf.getList("admin_channels")).contains(channel.toLowerCase()))
			return;
		
		Matcher m = sshRegex.matcher(message);
		if (!m.find())
			return;
		
		String server = m.group(1);
		int port = m.group(2) == null ? 22 : Integer.parseInt(m.group(2));
		String user = m.group(3);
		String ip = m.group(4);
		String pass = m.group(5);
		
		ServerInfo si = new ServerInfo();
		si.name = server;
		si.host = ip;
		si.user = user;
		si.pass = pass;
		si.port = port;
		si.protocol = "ssh";
		
		servercontrol.geoSSHInfo.put(server, si);
		log.log(Level.FINE, "Got geo SSH info for " + server);
		
		synchronized (servercontrol.geoSSHInfo)
		{
			servercontrol.geoSSHInfo.notify();
		}
	}
}
