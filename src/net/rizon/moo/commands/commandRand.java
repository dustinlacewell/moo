package net.rizon.moo.commands;

import java.util.Random;
import net.rizon.moo.command;
import net.rizon.moo.moo;
import net.rizon.moo.mpackage;
import net.rizon.moo.server;

public class commandRand extends command
{
	public commandRand(mpackage pkg)
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
		moo.notice(source, "Syntax: .rand [length]");
		moo.notice(source, "Generates a random string of the given length. If no");
		moo.notice(source, "length is given, it will default to 8, which is also");
		moo.notice(source, "the minimum length.");
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
			
			moo.reply(source, target, "Rand(" + randLen + ") = " + randomString(randLen));
		}
		catch (NumberFormatException e)
		{
			moo.reply(source, target, params[1] + " is not a number.");
		}
	}
}
