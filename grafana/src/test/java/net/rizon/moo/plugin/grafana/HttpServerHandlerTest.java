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
package net.rizon.moo.plugin.grafana;

import com.google.inject.Provider;
import com.google.inject.testing.fieldbinder.Bind;
import io.netty.channel.ChannelHandlerContext;
import java.util.Arrays;
import javax.inject.Inject;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.grafana.beans.GrafanaNotification;
import net.rizon.moo.plugin.grafana.beans.Metric;
import net.rizon.moo.plugin.grafana.conf.GrafanaConfiguration;
import net.rizon.moo.test.MooJUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MooJUnitRunner.class)
public class HttpServerHandlerTest
{
	@Mock
	private ChannelHandlerContext ctx;

	@Bind
	@Mock
	private Protocol protocol;

	@Inject
	private HttpServerHandler handler;

	@Mock
	private GrafanaConfiguration conf;

	@Bind
	private Provider<GrafanaConfiguration> confProvider = new Provider<GrafanaConfiguration>()
	{
		@Override
		public GrafanaConfiguration get()
		{
			return conf;
		}
	};

	@Test
	public void testChannelRead0() throws Exception
	{
		when(conf.getReportStates()).thenReturn(Arrays.asList("alerting"));

		GrafanaNotification notification = new GrafanaNotification();
		notification.setRuleName("test");
		notification.setState("alerting");
		notification.setEvalMatches(Arrays.asList(
			new Metric("42", "server.one", null),
			new Metric("43", "server.two", null)
		));

		handler.channelRead0(ctx, notification);

		verify(protocol).privmsg(Matchers.anyString(), Matchers.eq("Grafana: test is now in state alerting for metric(s): server.one (42), server.two (43)"));
	}

}
