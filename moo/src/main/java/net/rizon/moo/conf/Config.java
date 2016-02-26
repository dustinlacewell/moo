package net.rizon.moo.conf;

import java.util.List;

public class Config extends Configuration
{
	public boolean debug, protocol_debug;
	public String version;
	public String plugin_repository;
	public General general;
	public DatabaseConfiguration database;
	public String[] channels;
	public String[] dev_channels;
	public String[] spam_channels;
	public String[] flood_channels;
	public String[] split_channels;
	public String[] staff_channels;
	public String[] oper_channels;
	public String[] admin_channels;
	public String[] log_channels;
	public String[] moo_log_channels;
	public String[] kline_channels;
	public String[] help_channels;
	public Mail mail;
	public List<ConfPlugin> plugins;

	/**
	 * Loads the general configuration.
	 * @return Configuration settings.
	 * @throws Exception Thrown when something is wrong.
	 */
	public static Config load() throws Exception
	{
		return Configuration.load("moo.yml", Config.class);
	}

	/**
	 * Validates loaded configuration.
	 * @throws ConfigurationException When something is invalid.
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("Version number", version);
		Validator.validatePath("Plugin repository", plugin_repository);

		general.validate();
		database.validate();

		Validator.validateChannelList("Normal channels", channels);
		Validator.validateChannelList("Dev channels", dev_channels);
		Validator.validateChannelList("Spam channels", spam_channels);
		Validator.validateChannelList("Flood channels", flood_channels);
		Validator.validateChannelList("Split channels", split_channels);
		Validator.validateChannelList("Staff channels", staff_channels);
		Validator.validateChannelList("Oper channels", oper_channels);
		Validator.validateChannelList("Admin channels", admin_channels);
		Validator.validateChannelList("Log channels", log_channels);
		Validator.validateChannelList("Moo log channels", moo_log_channels);
		Validator.validateChannelList("KLine channels", kline_channels);
		Validator.validateChannelList("Help channels", help_channels);

		mail.validate();

		Validator.validateList(plugins);
	}

	/**
	 * Checks if the channel is in the channels list.
	 * @param channel Channel to check.
	 * @return True if the channel is in the list. False otherwise.
	 */
	public boolean channelsContains(String channel)
	{
		return containsIgnoreCase(channel, channels);
	}

	/**
	 * Checks if the channel is in the log channels list.
	 * @param channel Channel to check.
	 * @return True if the channel is in the list. False otherwise.
	 */
	public boolean logChannelsContains(String channel)
	{
		return containsIgnoreCase(channel, log_channels);
	}

	/**
	 * Checks if the channel is in the admin channels list.
	 * @param channel Channel to check.
	 * @return True if the channel is in the list. False otherwise.
	 */
	public boolean adminChannelsContains(String channel)
	{
		return containsIgnoreCase(channel, admin_channels);
	}

	/**
	 * Checks if the channel is in the oper channels list.
	 * @param channel Channel to check.
	 * @return True if the channel is in the list. False otherwise.
	 */
	public boolean operChannelsContains(String channel)
	{
		return containsIgnoreCase(channel, oper_channels);
	}
}
