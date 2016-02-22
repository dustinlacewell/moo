package net.rizon.moo.plugin.wiki;

import com.google.inject.Inject;
import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.rizon.moo.Moo;
import net.rizon.moo.conf.Config;
import net.rizon.moo.irc.Protocol;
import org.slf4j.Logger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

class WikiChecker extends Thread
{
	@Inject
	private static Logger logger;
	
	@Inject
	private Protocol protocol;
	
	@Inject
	private Config conf;
	
	private static final SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

	private static Date last;

	@Override
	public void run()
	{
		InputStream is = null;

		try
		{
			is = new URL(wiki.conf.url).openStream();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document document = db.parse(is);
			Date highest = null;

			NodeList nlist = document.getElementsByTagName("entry");
			for (int i = 0; i < nlist.getLength(); ++i)
			{
				Element n = (Element) nlist.item(i);

				String up = n.getElementsByTagName("updated").item(0).getTextContent();
				Date d = sm.parse(up);

				if (last != null && !d.after(last))
					continue;

				if (highest == null || d.after(highest))
					highest = d;

				if (last == null)
					continue;

				String title = n.getElementsByTagName("title").item(0).getTextContent(),
						author = n.getElementsByTagName("author").item(0).getTextContent(),
						link = n.getElementsByTagName("link").item(0).getAttributes().getNamedItem("href").getTextContent();

				protocol.privmsgAll(conf.help_channels, title + " modified by " + author + " (" + link + ")");
			}

			if (highest != null)
				last = highest;
		}
		catch (Exception ex)
		{
			logger.warn("Unable to check wiki", ex);
		}
		finally
		{
			try { is.close(); }
			catch (Exception ex) { }
		}
	}
}