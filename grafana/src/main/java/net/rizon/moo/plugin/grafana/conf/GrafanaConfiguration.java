/*
 * Copyright (c) 2016, Adam <Adam@rizon.net>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.rizon.moo.plugin.grafana.conf;

import java.io.IOException;
import java.util.List;
import net.rizon.moo.conf.Configuration;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.conf.Validator;

public class GrafanaConfiguration extends Configuration
{
	private static final String CONF_NAME = "grafana.yml";

	private String ip;
	private int port;
	private String reportChannel;
	private List<String> reportStates;

	public static GrafanaConfiguration load() throws ConfigurationException, IOException
	{
		return load(CONF_NAME, GrafanaConfiguration.class);
	}

	@Override
	public void validate() throws ConfigurationException
	{
		Validator.validateNotEmpty("ip", ip);
		Validator.validatePort("port", port, false);
		Validator.validateChannelName("reportChannel", reportChannel);
		Validator.validateNotEmpty("reportStates", reportStates);
	}

	public String getIp()
	{
		return ip;
	}

	public void setIp(String ip)
	{
		this.ip = ip;
	}

	public int getPort()
	{
		return port;
	}

	public void setPort(int port)
	{
		this.port = port;
	}

	public String getReportChannel()
	{
		return reportChannel;
	}

	public void setReportChannel(String reportChannel)
	{
		this.reportChannel = reportChannel;
	}

	public List<String> getReportStates()
	{
		return reportStates;
	}

	public void setReportStates(List<String> reportStates)
	{
		this.reportStates = reportStates;
	}

}
