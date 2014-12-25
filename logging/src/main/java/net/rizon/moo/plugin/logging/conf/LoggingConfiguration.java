package net.rizon.moo.plugin.logging.conf;

import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class LoggingConfiguration extends Configuration
{
	public String path, filename, date;
	public int searchDays;
	
	public static LoggingConfiguration load() throws Exception
	{
		return load("logging.yml", LoggingConfiguration.class);
	}
	
	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("path", path);
		Validator.validateNotEmpty("filename", filename);
		Validator.validateNotEmpty("date", date);
		Validator.validateNotZero("searchDays", searchDays);
	}
}
