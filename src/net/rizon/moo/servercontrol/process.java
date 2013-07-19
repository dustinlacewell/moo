package net.rizon.moo.servercontrol;

public abstract class process extends Thread
{
	protected connection con;
	private String command;

	public process(connection con, final String command)
	{
		this.con = con;
		this.command = command;
		
		this.con.processes.add(this);
	}
	
	@Override
	public void run()
	{
		try
		{
			this.con.lock.lock();
			
			if (this.con.isConnected() == false)
				this.con.connect();
			
			this.con.execute(this.command);
			
			for (String in; (in = this.con.readLine()) != null;)
				if (in.trim().isEmpty() == false)
					this.onLine(in);
		}
		catch (Exception ex)
		{
			this.onError(ex);
			ex.printStackTrace();
		}
		finally
		{
			this.con.lock.unlock();
			this.con.processes.remove(this);
		}
		
		this.onFinish();
	}
	
	public abstract void onLine(String in);
	public void onError(Exception e) { }
	public void onFinish() { }
}
