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
import com.google.inject.Provides;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import net.rizon.moo.Command;
import net.rizon.moo.Moo;
import net.rizon.moo.Plugin;
import net.rizon.moo.conf.ConfigurationException;
import net.rizon.moo.plugin.grafana.conf.GrafanaConfiguration;

public class Grafana extends Plugin
{
	@Inject
	private Moo moo;

	@Inject
	private HttpServerInitializer handler;

	private Channel channel;
	private GrafanaConfiguration conf;

	public Grafana()
	{
		super("Grafana", "Shows Grafana notifications");
	}

	@Override
	public void start() throws ConfigurationException, IOException
	{
		conf = GrafanaConfiguration.load();

		ServerBootstrap bootstrap = new ServerBootstrap()
			.group(moo.getGroup())
			.channel(NioServerSocketChannel.class)
			.childHandler(handler);

		channel = bootstrap.bind(conf.getIp(), conf.getPort()).channel();
	}

	@Override
	public void stop()
	{
		channel.close().awaitUninterruptibly();
	}

	@Override
	public List<Command> getCommands()
	{
		return Collections.EMPTY_LIST;
	}

	@Override
	protected void configure()
	{
		bind(Grafana.class).toInstance(this);

		bind(HttpServerInitializer.class);
		bind(HttpServerHandler.class);
	}

	@Provides
	public GrafanaConfiguration provideConfig()
	{
		return conf;
	}

}
