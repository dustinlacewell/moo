package net.rizon.moo;

import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class Version
{
	private static final Logger logger = LoggerFactory.getLogger(Version.class);
	
	public static String getRevision()
	{
		return "FIX THIS";
//		try
//		{
//			Manifest mf = new Manifest();
//			mf.read(Version.class.getResourceAsStream("META-INF/MANIFEST.MF"));
//
//			Attributes att = mf.getMainAttributes();
//			return att.getValue("Build-Number");
//		}
//		catch (IOException ex)
//		{
//			logger.warn("unable to get revision", ex);
//			return "UNKNOWN";
//		}
	}
}
