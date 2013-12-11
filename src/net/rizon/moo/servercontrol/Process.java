package net.rizon.moo.servercontrol;

public abstract class Process extends Thread
{
	protected Connection con;

	public Process(Connection con)
	{
		this.con = con;
		con.processes.add(this);
	}
	
	@Override
	final public void run()
	{
		try
		{
			synchronized (this.con)
			{
				this.onRun();
			}
		}
		finally
		{
			this.con.processes.remove(this);
		}
	}
	
	public abstract void onRun();
}
