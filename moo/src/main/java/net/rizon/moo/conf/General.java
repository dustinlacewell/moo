package net.rizon.moo.conf;

public class General extends Configuration
{
	public String nick, ident, realname, server, host, server_pass, cert;
	public Protocol protocol;
	public Oper oper;
	public NickServ nickserv;
	public int port;
	public boolean ssl;

	/**
	 * Validates the General settings loaded from the YAML file.
	 * @throws ConfigurationException
	 */
	@Override
	public void validate() throws ConfigurationException
	{
		// Mandatory settings.
		Validator.validateNotEmpty("nick", nick);
		Validator.validateNotEmpty("ident", ident);
		Validator.validateNotEmpty("realname", realname);
		Validator.validateNotNull("protocol", protocol);
		Validator.validateHost("server", server);
		Validator.validatePort("port", port, true);

		// Optional settings
		Validator.validateNullOrHost("host", host);
		Validator.validateNullOrNotEmpty("server password", server_pass);
		Validator.validateNullOrValid("NickServ", nickserv);
		Validator.validateNullOrValid("Oper", oper);
	}
}
