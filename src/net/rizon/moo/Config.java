package net.rizon.moo;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class Config
{
	private Properties prop;
	
	public Config() throws IOException
	{
		prop = new Properties();
		FileInputStream fis = new FileInputStream("moo.properties");
		prop.load(fis);
	}
	
	public String getString(String name)
	{
		String value = prop.getProperty(name);
		return value != null ? value : "";
	}
	
	public int getInt(String name)
	{
		try
		{
			return Integer.parseInt(getString(name));
		}
		catch (NumberFormatException ex)
		{
			return 0;
		}
	}
	
	public boolean getBool(String name)
	{
		String value = getString(name);
		return !value.isEmpty() && !value.equalsIgnoreCase("no") && !value.equalsIgnoreCase("false") && !value.equals("0");
	}
	
	public String[] getList(String name)
	{
		String value = getString(name);
		return value.split("[, ]");
	}
	
	public boolean listContains(String name, String value)
	{
		for (String s : getList(name))
			if (s.equalsIgnoreCase(value))
				return true;
		return false;
	}
	
	public void setBoolean(String name, boolean value)
	{
		prop.setProperty(name, value ? "true" : "false");
	}
}
