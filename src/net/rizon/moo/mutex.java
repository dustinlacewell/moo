package net.rizon.moo;

public class mutex
{
	private volatile boolean locked;
	
	public synchronized void lock()
	{
		synchronized (this)
		{
			while (locked)
			{
				try
				{
					this.wait();
				}
				catch (InterruptedException e) { }
			}
		}
		
		locked = true;
	}
	
	public void unlock()
	{
		locked = false;
		
		synchronized (this)
		{
			this.notify();
		}
	}
}