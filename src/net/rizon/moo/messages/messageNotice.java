package net.rizon.moo.messages;

import java.util.Iterator;

import net.rizon.moo.mail;
import net.rizon.moo.message;
import net.rizon.moo.moo;
import net.rizon.moo.server;

public class messageNotice extends message
{
	public messageNotice()
	{
		super("NOTICE");
	}

	@Override
	public void run(String source, String[] message)
	{
		if (source.equals("NickServ!service@rizon.net"))
		{
			if (message.length > 1 && message[1].indexOf("This nickname is registered") != -1 && moo.conf.getNickServPass() != null && moo.conf.getNickServPass().isEmpty() == false)
				moo.sock.privmsg(source, "IDENTIFY " + moo.conf.getNickServPass());
		}
		else if (source != null && source.indexOf('.') != -1 && message.length > 1)
		{
			if (message[1].indexOf("being introduced by") != -1)
			{
				String[] tokens = message[1].split(" ");
				
				server serv = server.findServerAbsolute(tokens[4]);
				if (serv == null)
				{
					serv = new server(tokens[4]);
					moo.sock.write("MAP");
				}
				else
					serv.splitDel();
				serv.link(tokens[8]);
				
				serv = server.findServerAbsolute(tokens[8]);
				if (serv == null)
					serv = new server(tokens[8]);
				else
					serv.splitDel();
				serv.link(tokens[4]);
				
				if (moo.conf.getSplitEmail() != null && moo.conf.getSplitEmail().isEmpty() == false)
					mail.send(moo.conf.getSplitEmail(), "Server introduced", tokens[4] + " introduced by " + tokens[8]);
			}
			else if (message[1].indexOf("End of burst from") != -1)
			{
				String[] tokens = message[1].split(" ");
				server serv = server.findServerAbsolute(tokens[7]);
				if (serv == null)
				{
					serv = new server(tokens[7]);
					moo.sock.write("MAP");
				}
				else
					serv.splitDel();
				serv.link(source);
				
				serv = server.findServerAbsolute(source);
				if (serv == null)
					new server(source);
				else
					serv.splitDel();
				serv.link(tokens[7]);
				
				if (moo.conf.getSplitEmail() != null && moo.conf.getSplitEmail().isEmpty() == false)
					mail.send(moo.conf.getSplitEmail(), "Server introduced", tokens[7] + " introduced");
			}
			else if (message[1].indexOf("split from") != -1)
			{
				String[] tokens = message[1].split(" ");
				server serv = server.findServerAbsolute(tokens[7]);
				if (serv != null)
					serv.links.remove(tokens[4]);
				serv = server.findServerAbsolute(tokens[4]);
				if (serv == null)
					serv = new server(tokens[4]);
				serv.split(tokens[7]);
				
				if (moo.conf.getDisableSplitMessage() == false)
				{
					if (moo.conf.getSplitChannels() != null)
						for (int i = 0; i < moo.conf.getSplitChannels().length; ++i)
							moo.sock.privmsg(moo.conf.getSplitChannels()[i], tokens[4] + " split from " + tokens[7]);
					for (Iterator<server> it = server.getServers().iterator(); it.hasNext();)
					{
						server s = it.next();
						if (s.isSplit() == false && s.isHub() == true)
							for (Iterator<String> it2 = s.clines.iterator(); it2.hasNext();)
							{
								String cline = it2.next();
								
								if (serv.getName().equalsIgnoreCase(cline))
								{
									if (moo.conf.getSplitChannels() != null)
										for (int i = 0; i < moo.conf.getSplitChannels().length; ++i)
											moo.sock.privmsg(moo.conf.getSplitChannels()[i], serv.getName() + " can connect to " + s.getName());
								}
							}
					}
				}
				if (moo.conf.getSplitEmail() != null && moo.conf.getSplitEmail().isEmpty() == false)
					mail.send(moo.conf.getSplitEmail(), "Server split", serv.getName() + " split from " + tokens[7]);
			}
		}
	}
}
