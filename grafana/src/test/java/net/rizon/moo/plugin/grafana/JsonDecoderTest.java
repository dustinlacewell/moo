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

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.FullHttpRequest;
import java.util.ArrayList;
import java.util.List;
import net.rizon.moo.plugin.grafana.beans.GrafanaNotification;
import net.rizon.moo.test.MooJUnitRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

@RunWith(MooJUnitRunner.class)
public class JsonDecoderTest
{
	private static final String NOTIFICATION = "{\n"
		+ "	\"evalMatches\": [{\n"
		+ "		\"value\": 80.98168494963444,\n"
		+ "		\"metric\": \"uworld.hub (/)\",\n"
		+ "		\"tags\": null\n"
		+ "	}],\n"
		+ "	\"ruleId\": 4,\n"
		+ "	\"ruleName\": \"High Disk Usage (\\u003e90%)\",\n"
		+ "	\"ruleUrl\": \"http://grafana/dashboard/db/alerts?fullscreen\\u0026edit\\u0026tab=alert\\u0026panelId=4\",\n"
		+ "	\"state\": \"alerting\",\n"
		+ "	\"title\": \"[Alerting] High Disk Usage (\\u003e90%)\"\n"
		+ "}";

	@Mock
	private FullHttpRequest request;

	@Test
	public void testDecode() throws Exception
	{
		when(request.content()).thenReturn(Unpooled.wrappedBuffer(NOTIFICATION.getBytes()));

		JsonDecoder decoder = new JsonDecoder(GrafanaNotification.class);

		List<Object> objects = new ArrayList<>();
		decoder.decode(null, request, objects);

		GrafanaNotification notification = (GrafanaNotification) objects.get(0);

		Assert.assertEquals("[Alerting] High Disk Usage (>90%)", notification.getTitle());
		Assert.assertEquals(1, notification.getEvalMatches().size());
	}

}
