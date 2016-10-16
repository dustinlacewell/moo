package net.rizon.moo.plugin.tickets;

import java.util.Date;

class Ticket
{
	private String lastReplier;
	private Date nextReminder;
	private int reminded;
	private TicketState state;

	public String getLastReplier()
	{
		return lastReplier;
	}

	public void setLastReplier(String lastReplier)
	{
		this.lastReplier = lastReplier;
	}

	public Date getNextReminder()
	{
		return nextReminder;
	}

	public void setNextReminder(Date nextReminder)
	{
		this.nextReminder = nextReminder;
	}

	public int getReminded()
	{
		return reminded;
	}

	public void setReminded(int reminded)
	{
		this.reminded = reminded;
	}

	public TicketState getState()
	{
		return state;
	}

	public void setState(TicketState state)
	{
		this.state = state;
	}
}
