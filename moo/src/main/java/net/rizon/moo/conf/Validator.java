package net.rizon.moo.conf;

import java.io.File;
import java.util.Collection;
import java.util.List;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;

public class Validator
{
	private static EmailValidator ev = EmailValidator.getInstance(false);
	private static UrlValidator uv = new UrlValidator();

	/**
	 * Validates entire list
	 * @param <T> Class of list elements.
	 * @param list List with validatable elements.
	 * @throws ConfigurationException Thrown when an element is not valid.
	 */
	public static <T extends Validatable> void validateList(List<T> list) throws ConfigurationException
	{
		if (list == null)
			return;
		for (T t : list)
			t.validate();
	}

	/**
	 * Checks if object is defined.
	 * @param <T> Class of object.
	 * @param name Configuration item name.
	 * @param obj Configuration item to validate.
	 * @throws ConfigurationException Thrown when object is null.
	 */
	public static <T> void validateNotNull(String name, T obj) throws ConfigurationException
	{
		if (obj == null)
			throw new ConfigurationException(name + " must be defined");
	}

	/**
	 * Validates if the string is not empty.
	 * @param name Configuration item name.
	 * @param obj Configuration item.
	 * @throws ConfigurationException Thrown when string is null or empty.
	 */
	public static void validateNotEmpty(String name, String obj) throws ConfigurationException
	{
		validateNotNull(name, obj);
		if (obj.isEmpty())
			throw new ConfigurationException(name + " must not be empty");
	}

	/**
	 * Validates if collection is not empty.
	 * @param <T> Class of {@link Collection}.
	 * @param name Configuration item name.
	 * @param obj Collection.
	 * @throws ConfigurationException Thrown when {@link Collection} is empty.
	 */
	public static <T extends Collection<?>> void validateNotEmpty(String name, T obj) throws ConfigurationException
	{
		validateNotNull(name, obj);
		if (obj.isEmpty())
			throw new ConfigurationException(name + " must not be empty");
	}

	/**
	 * Validates if integer is not zero.
	 * @param name Configuration item name.
	 * @param i Integer to validate.
	 * @throws ConfigurationException Thrown when integer is 0.
	 */
	public static void validateNotZero(String name, int i) throws ConfigurationException
	{
		if (i == 0)
			throw new ConfigurationException(name + " must be non zero");
	}

	/**
	 * Validates if integer is positive (0 or higher)
	 * @param name Configuration item name.
	 * @param i Integer to validate
	 * @throws ConfigurationException Throw when integer is smaller than 0.
	 */
	public static void validatePositive(String name, int i) throws ConfigurationException
	{
		if (i < 0)
			throw new ConfigurationException(name + " must be 0 or higher");
	}

	/**
	 * Validates if this integer is a valid port number.
	 * @param name Configuration item name.
	 * @param i Port number.
	 * @param outgoing Specifies if this port is used to connect to another computer (true) or create a local socket (false).
	 * @throws ConfigurationException Thrown when the integer is not a valid port.
	 */
	public static void validatePort(String name, int i, boolean outgoing) throws ConfigurationException
	{
		int lowerBound = outgoing ? 1 : 0;
		if (i < lowerBound || i > 65535)
			if (i == 0 && outgoing)
				throw new ConfigurationException(name + " (" + i + ") is not a valid port for outgoing connections");
			else
				throw new ConfigurationException(name + " (" + i + ") is not a valid port");
	}

	/**
	 * Checks if the specified Host name or IP Address is valid.
	 * @param name Configuration item name.
	 * @param obj Host name or IP Address.
	 * @throws ConfigurationException When Host name or IP Address is invalid.
	 */
	public static void validateHost(String name, String obj) throws ConfigurationException
	{
		validateNotEmpty(name, obj);
	}

	public static void validateHostList(String name, String[] obj) throws ConfigurationException
	{
		validateNotNull(name, obj);
		for (String s : obj)
			validateHost(name, s);
	}

	/**
	 * Checks if optional configuration item Host is not defined or valid if defined.
	 * @param name Configuration item name.
	 * @param obj Host name or IP Address. (Or null)
	 * @throws ConfigurationException When Host name or IP Address is invalid if defined.
	 */
	public static void validateNullOrHost(String name, String obj) throws ConfigurationException
	{
		if (obj != null)
			validateHost(name, obj);
	}

	/**
	 * Checks if optional configuration item is not defined, or valid if defined.
	 * @param name Configuration item name.
	 * @param obj Item. (Or null)
	 * @throws ConfigurationException When configuration item is not valid if defined.
	 */
	public static void validateNullOrNotEmpty(String name, String obj) throws ConfigurationException
	{
		if (obj != null)
			Validator.validateNotEmpty(name, obj);
	}

	/**
	 * Checks if optional configuration item is not defined, or valid if defined.
	 * @param <T> Class of configuration item. (Must extend {@link Validatable})
	 * @param name Configuration item name.
	 * @param obj Object. (Or null)
	 * @throws ConfigurationException When configuration item is not valid if defined.
	 */
	public static <T extends Validatable> void validateNullOrValid(String name, T obj) throws ConfigurationException
	{
		if (obj != null)
			obj.validate();
	}

	/**
	 * Checks if the IRCMask is RFC1459 compliant.
	 * @param name Configuration item name.
	 * @param mask IRC Mask.
	 * @throws ConfigurationException When configuration item is null or not valid.
	 */
	public static void validateIRCMask(String name, String mask) throws ConfigurationException
	{
		// XXX ?
	}

	/**
	 * Checks if the list of channels is RFC1459 compliant.
	 * @param name Configuration item name.
	 * @param channels List of channels.
	 * @throws ConfigurationException When a channel is invalid or not RFC1459 compliant.
	 */
	public static void validateChannelList(String name, String[] channels) throws ConfigurationException
	{
		if (channels == null)
			return;
		
		for (String c : channels)
			validateChannelName(name, c);
	}

	/**
	 * Checks if the list of channels is RFC1459 compliant.
	 * <p>
	 * @param name     Configuration item name.
	 * @param channels List of channels.
	 * <p>
	 * @throws ConfigurationException When a channel is invalid or not RFC1459
	 *                                compliant.
	 */
	public static void validateChannelList(String name, List<String> channels) throws ConfigurationException
	{
		if (channels == null)
			return;
		
		for (String c : channels)
		{
			validateChannelName(name, c);
		}
	}

	/**
	 * Checks if channel name is RFC1459 compliant.
	 * @param name Configuration item name.
	 * @param channel Channel name.
	 * @throws ConfigurationException When the channel name is invalid or not RFC compliant.
	 */
	public static void validateChannelName(String name, String channel) throws ConfigurationException
	{
		validateNotEmpty(name, channel);
		// TODO: Better check :D
		if (channel.charAt(0) != '#')
			throw new ConfigurationException(name + " (" + channel + ") not a  compliant channel name.");
	}

	/**
	 * Checks if the list of email addresses is valid.
	 * @param name Configuration item name.
	 * @param emails List of email addresses.
	 * @throws ConfigurationException When an email is invalid.
	 */
	public static void validateEmailList(String name, List<String> emails) throws ConfigurationException
	{
		for (String email : emails)
			validateEmail(name, email);
	}

	/**
	 * Checks if the email address is valid.
	 * @param name Configuration item name.
	 * @param email Email address.
	 * @throws ConfigurationException When email is invalid.
	 */
	public static void validateEmail(String name, String email) throws ConfigurationException
	{
		validateNotEmpty(name, email);
		if (!ev.isValid(email))
			throw new ConfigurationException(name + " (" + email + ") not a valid email address");
	}

	/**
	 * Checks if specified path is correct for the current OS.
	 * This sounds more fancy than it is, it actually just checks if it exists!
	 * @param name Configuration item name.
	 * @param path Path to check.
	 * @throws ConfigurationException When path is non-existent.
	 */
	public static void validatePath(String name, String path) throws ConfigurationException
	{
		validateNotNull(name, path);
		File f = new File(path);
		if (f.exists() && !f.canRead())
			throw new ConfigurationException(name + " (" + path + ") is not readable");
	}

	/**
	 * Checks if all paths are correct for the current OS.
	 * @param name Configuration item name.
	 * @param paths Paths to check.
	 * @throws ConfigurationException When path is non-existent.
	 */
	public static void validatePathList(String name, String[] paths) throws ConfigurationException
	{
		validateNotNull(name, paths);
		for (String s : paths)
			validatePath(name, s);
	}

	/**
	 * Checks if a list of strings does not contain empty elements.
	 * @param name Configuration item name.
	 * @param list List of strings.
	 * @throws ConfigurationException When a string is empty.
	 */
	public static void validateStringList(String name, List<String> list) throws ConfigurationException
	{
		for (String s : list)
			validateNotEmpty(name, s);
	}

	/**
	 * Checks if the URL is valid.
	 * @param name Configuration item name.
	 * @param url URL to check.
	 * @throws ConfigurationException When URL is not valid.
	 */
	public static void validateURL(String name, String url) throws ConfigurationException
	{
		if (!uv.isValid(url))
			throw new ConfigurationException(name + " (" + url + ") is not a valid URL.");
	}

	/**
	 * Checks if the list of URLs is valid.
	 * @param name Configuration item name.
	 * @param urls URLs to check.
	 * @throws ConfigurationException When a URL is not valid.
	 */
	public static void validateURLList(String name, List<String> urls) throws ConfigurationException
	{
		validateNotNull(name, urls);
		for (String s : urls)
			validateURL(name, s);
	}
}
