package net.rizon.moo;

import java.util.ArrayList;
import java.util.Date;

public abstract class timer
{
	private static ArrayList<timer> timers = new ArrayList<timer>();

	public Date creation;
	public Date tick;
	public long time_from_now;
	public boolean repeating;

	public timer(long time_from_now, boolean repeating)
	{
		this.creation = new Date();
		this.tick = new Date(System.currentTimeMillis() + (time_from_now * 1000));
		this.time_from_now = time_from_now;
		this.repeating = repeating;	
	}
	
	public void start()
	{
		timers.add(this);
	}
	
	public void stop()
	{
		timers.remove(this);
	}
	
	public abstract void run(final Date now);
	
	public static void processTimers()
	{
		Date now = new Date();

		for (int i = timers.size(); i >  0; --i)
		{
			timer t = timers.get(i - 1);
			
			if (now.after(t.tick))
			{
				try
				{
					t.run(now);
				}
				catch (Exception ex)
				{
					System.out.println("Error running timer " + t.toString());
					ex.printStackTrace();
				}
				
				if (t.repeating == false)
					t.stop();
				else
					t.tick = new Date(System.currentTimeMillis() + (t.time_from_now * 1000));
			}
		}
	}
}