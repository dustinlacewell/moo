package net.rizon.moo.plugin.core;

import com.google.inject.Inject;
import java.util.Random;

import net.rizon.moo.Command;
import net.rizon.moo.CommandSource;
import net.rizon.moo.Plugin;

class CommandRand extends Command
{
	@Inject
	CommandRand()
	{
		super(".RAND", "Generate a random string");
	}

	private static String randChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@_#^&%$";
	private static String randomString(int length)
	{
		Random rand = new Random();
		StringBuilder rS = new StringBuilder();
		for (int i = 0; i < length; i++)
			rS.append(randChars.charAt(rand.nextInt(randChars.length())));

		return rS.toString();
	}

	@Override
	public void onHelp(CommandSource source)
	{
		source.notice("Syntax: .rand [length]");
		source.notice("Generates a random string of the given length. If no");
		source.notice("length is given, it will default to 8, which is also");
		source.notice("the minimum length.");
	}

	@Override
	public void execute(CommandSource source, String[] params)
	{
		try
		{
			int randLen = 8;

			if(params.length > 1)
				randLen = Integer.parseInt(params[1]);
			if (randLen > 300)
				randLen = 300;
			if (randLen < 8)
				randLen = 8;

			source.reply("Rand(" + randLen + ") = " + randomString(randLen));
		}
		catch (NumberFormatException e)
		{
			source.reply(params[1] + " is not a number.");
		}
	}
}
