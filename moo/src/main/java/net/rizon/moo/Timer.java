package net.rizon.moo;

import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

public abstract class Timer
{
	private static final Logger log = Logger.getLogger(Timer.class.getName());
	private static ArrayList<Timer> timers = new ArrayList<Timer>();

	private Date tick;
	private long time_from_now;
	private boolean repeating;

	public Timer(long time_from_now, boolean repeating)
	{
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
	
	public void setRepeating(boolean r)
	{
		this.repeating = r;
	}
	
	public final Date getTick()
	{
		return this.tick;
	}
	
	public abstract void run(final Date now);
	
	public static void processTimers()
	{
		Date now = new Date();

		for (int i = timers.size(); i >  0; --i)
		{
			Timer t = timers.get(i - 1);
			
			if (now.after(t.tick))
			{
				try
				{
					t.run(now);
				}
				catch (Exception ex)
				{
					log.log(Level.WARNING, "Error running timer " + t.toString(), ex);
				}
				
				if (t.repeating == false)
					t.stop();
				else
					t.tick = new Date(System.currentTimeMillis() + (t.time_from_now * 1000));
			}
		}
	}
}