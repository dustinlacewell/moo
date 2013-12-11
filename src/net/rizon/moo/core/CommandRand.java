package net.rizon.moo.core;

import java.util.Random;
import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.MPackage;

class CommandRand extends Command
{
	public CommandRand(MPackage pkg)
	{
		super(pkg, ".RAND", "Generate a random string");
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
	public void onHelp(String source)
	{
		Moo.notice(source, "Syntax: .rand [length]");
		Moo.notice(source, "Generates a random string of the given length. If no");
		Moo.notice(source, "length is given, it will default to 8, which is also");
		Moo.notice(source, "the minimum length.");
	}

	@Override
	public void execute(String source, String target, String[] params)
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
			
			Moo.reply(source, target, "Rand(" + randLen + ") = " + randomString(randLen));
		}
		catch (NumberFormatException e)
		{
			Moo.reply(source, target, params[1] + " is not a number.");
		}
	}
}
