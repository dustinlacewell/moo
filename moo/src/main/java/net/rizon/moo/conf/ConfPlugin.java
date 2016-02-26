package net.rizon.moo.conf;

public class ConfPlugin implements Validatable
{
	public String groupId;
	public String artifactId;
	public String version;

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("groupId", groupId);
		Validator.validateNotEmpty("artifactId", artifactId);
		Validator.validateNotEmpty("version", version);
	}
}
