package net.rizon.moo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.logging.Level;

public class version
{
	private static final logger log = logger.getLogger(version.class.getName());
	private static HashMap<String, String> values = new HashMap<String, String>();
	
	public static void load()
	{
		try
		{
			String[] cmd = { "/bin/sh", "-c", "git --no-pager log -n 1 | grep \"^[a-zA-Z]\"" };
			Process proc = Runtime.getRuntime().exec(cmd);
			BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
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
			proc.getOutputStream().close();
			proc.getErrorStream().close();
			proc.destroy();
			
			String[] cmd2 = { "/bin/sh", "-c", "git --no-pager log | grep \"^commit\" | wc -l" };
			proc = Runtime.getRuntime().exec(cmd2);
			br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
			
			for (String line; (line = br.readLine()) != null;)	
				version.values.put("revision", line);
					
			br.close();
			proc.getOutputStream().close();
			proc.getErrorStream().close();
			proc.destroy();
		}
		catch (IOException ex)
		{
			log.log(Level.INFO, "Unable to load GIT version data", ex);
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
