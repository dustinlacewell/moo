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

import com.google.inject.Inject;
import com.google.inject.Provider;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.rizon.moo.irc.Protocol;
import net.rizon.moo.plugin.grafana.beans.GrafanaNotification;
import net.rizon.moo.plugin.grafana.beans.Metric;
import net.rizon.moo.plugin.grafana.conf.GrafanaConfiguration;

@Sharable
public class HttpServerHandler extends SimpleChannelInboundHandler<GrafanaNotification>
{
	@Inject
	private Protocol protocol;

	@Inject
	private Provider<GrafanaConfiguration> providerConfig;

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, GrafanaNotification notification) throws Exception
	{
		GrafanaConfiguration conf = providerConfig.get();

		if (conf.getReportStates().contains(notification.getState()) == false || notification.getEvalMatches().isEmpty())
		{
			return;
		}
		
		StringBuilder builder = new StringBuilder();
		for (Metric metric : notification.getEvalMatches())
		{
			builder.append(metric.getMetric()).append(" (").append(metric.getValue()).append("), ");
		}
		
		String message = builder.substring(0, builder.length() - 2);

		protocol.privmsg(conf.getReportChannel(), "Grafana: " + notification.getRuleName() + " is now in state " + notification.getState() + " for metric(s): " + message);

		ctx.close();
	}
}
