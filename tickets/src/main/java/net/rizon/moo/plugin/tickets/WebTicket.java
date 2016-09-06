package net.rizon.moo.plugin.tickets;

class WebTicket
{
	private int ticket;
	private String ip;
	private String real_ip;
	private String contact_name;
	private String contact_email;
	private String ban_message;
	private int host_number;
	private String why;
	private String added;
	private String status;
	private String akill;
	private String assignee;
	private String assignee_name;
	private String last_replier;

	public int getTicket()
	{
		return ticket;
	}

	public String getIp()
	{
		return ip;
	}

	public String getRealIp()
	{
		return real_ip;
	}

	public String getContactName()
	{
		return contact_name;
	}

	public String getContactEmail()
	{
		return contact_email;
	}

	public String getBanMessage()
	{
		return ban_message;
	}

	public int getHostNumber()
	{
		return host_number;
	}

	public String getWhy()
	{
		return why;
	}

	public String getAdded()
	{
		return added;
	}

	public String getStatus()
	{
		return status;
	}

	public String getAkill()
	{
		return akill;
	}

	public String getAssignee()
	{
		return assignee;
	}

	public String getAssigneeName()
	{
		return assignee_name;
	}

	public String getLastReplier()
	{
		return last_replier;
	}
}
