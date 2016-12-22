package net.rizon.moo.conf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

public abstract class Configuration implements Validatable
{
	/**
	 * Loads the specified file as a YAML configuration.
	 *
	 * @param <T>
	 * @param file File.
	 * @param c Class of configuration file.
	 * @return Loaded configuration.
	 * @throws net.rizon.moo.conf.ConfigurationException
	 * @throws java.io.IOException
	 */
	public static <T extends Configuration> T load(final String file, final Class<T> c) throws ConfigurationException, IOException
	{
		Yaml yaml = new Yaml(new CustomClassLoaderConstructor(c, c.getClassLoader()));
		try (InputStream io = new FileInputStream(file))
		{
			@SuppressWarnings("unchecked")
			T conf = (T) yaml.load(io);
			conf.validate();
			return conf;
		}
	}

	// TODO: Below classes should really be in some utilities.
	/**
	 * Checks if the string is in the list, ignoring case.
	 *
	 * @param text String to check.
	 * @param list List of Strings.
	 * @return True if string is in list, False otherwise.
	 */
	public static boolean containsIgnoreCase(final String text, final List<String> list)
	{
		for (String s : list)
		{
			if (s.equalsIgnoreCase(text))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks if the string is in the array, ignoring case.
	 *
	 * @param text String to check.
	 * @param array Array of Strings.
	 * @return True if string is in array, False otherwise.
	 */
	public static boolean containsIgnoreCase(final String text, final String[] array)
	{
		for (String s : array)
		{
			if (s.equalsIgnoreCase(text))
			{
				return true;
			}
		}
		return false;
	}
}
