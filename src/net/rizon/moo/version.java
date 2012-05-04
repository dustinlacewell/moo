package net.rizon.moo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

public class version
{
	private static HashMap<String, String> values = new HashMap<String, String>();
	
	public static void load()
	{
		try
		{
			String[] cmd = { "/bin/sh", "-c", "git --no-pager log -n 1 | grep \"^[a-zA-Z]\"" };
			InputStream is = Runtime.getRuntime().exec(cmd).getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			
			for (String line; (line = br.readLine()) != null;)
			{
				line = line.trim();
				line = line.replaceAll(":", "");
				while (line.indexOf("  ") != -1)
					line = line.replaceAll("  ", " ");

				int sp = line.indexOf(' ');
				if (sp == -1)
					return;

				String name = line.substring(0, sp);
				String value = line.substring(sp + 1);
						
				version.values.put(name, value);
			}
			
			br.close();
			is.close();
			
			String[] cmd2 = { "/bin/sh", "-c", "git --no-pager log | grep \"^commit\" | wc -l" };
			is = Runtime.getRuntime().exec(cmd2).getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			
			for (String line; (line = br.readLine()) != null;)	
				version.values.put("revision", line);
					
			br.close();
			is.close();
		}
		catch (IOException ex)
		{
			System.out.println("Unable to load GIT version data");
			ex.printStackTrace();
		}
	}
	
	private static final String getAttribute(final String attribute)
	{
		return version.values.get(attribute);
	}
	
	public static final String getFullVersion()
	{
		String lastRev = version.getAttribute("revision");
		String lastDate = version.getAttribute("Date");
		String lastAuthor = version.getAttribute("Author");
		
		if (lastRev == null || lastDate == null || lastAuthor == null)
			return "Unknown";

		String fullVersion = lastRev + " on " + lastDate + " by " + lastAuthor;
		fullVersion = fullVersion.trim();

		return fullVersion;
	}
}
