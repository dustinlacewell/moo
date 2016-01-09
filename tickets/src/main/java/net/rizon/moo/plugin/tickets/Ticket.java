package net.rizon.moo.plugin.tickets;

import java.util.Date;

class Ticket
{
	public String lastReplier;
	public Date nextReminder;
	public int reminded;
	public TicketState state;
}
