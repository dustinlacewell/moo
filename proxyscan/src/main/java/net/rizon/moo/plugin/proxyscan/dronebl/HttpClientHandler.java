/*
 * Copyright (c) 2017, Adam <Adam@rizon.net>
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
package net.rizon.moo.plugin.proxyscan.dronebl;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.QueryStringEncoder;
import java.net.URI;
import java.net.URISyntaxException;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;

public class HttpClientHandler extends SimpleChannelInboundHandler<HttpObject>
{
	private static final String RPC = "/rpc";

	private final ProxyscanConfiguration config;
	private final BlacklistedIP blacklist;

	public HttpClientHandler(ProxyscanConfiguration config, BlacklistedIP blacklist)
	{
		this.config = config;
		this.blacklist = blacklist;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws URISyntaxException
	{
		QueryStringEncoder encoder = new QueryStringEncoder(RPC);
		encoder.addParam("rpckey", config.getDronebl().getRpcKey());
		encoder.addParam("ip", blacklist.getIp());
		encoder.addParam("type", "" + blacklist.getReportas());
		encoder.addParam("comment", blacklist.getComment());
		encoder.addParam("port", "" + blacklist.getPort());

		URI uriGet = new URI(encoder.toString());
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,uriGet.toASCIIString());
		HttpHeaders headers = request.headers();

		headers.set(HttpHeaderNames.HOST, config.getDronebl().getDroneblHost());
		headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);

		ctx.writeAndFlush(request).addListener(ChannelFutureListener.CLOSE);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, HttpObject notification) throws Exception
	{
		ctx.close();
	}
}
