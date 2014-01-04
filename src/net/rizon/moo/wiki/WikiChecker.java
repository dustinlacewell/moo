package net.rizon.moo.wiki;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import net.rizon.moo.Logger;
import net.rizon.moo.Moo;

class WikiChecker extends Thread
{
	private static final SimpleDateFormat sm = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
	private static final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	
	private static Date last;

	@Override
	public void run()
	{
		InputStream is = null;
		
		try
		{
			is = new URL(Moo.conf.getString("wiki.url")).openStream();
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
				
				for (String c : Moo.conf.getList("help_channels"))
					Moo.privmsg(c, title + " modified by " + author + " (" + link + ")");
			}
			
			if (highest != null)
				last = highest;
		}
		catch (Exception ex)
		{
			Logger.getGlobalLogger().log(Level.WARNING, "Unable to check wiki", ex);
		}
		finally
		{
			try { is.close(); }
			catch (Exception ex) { }
		}
	}
}