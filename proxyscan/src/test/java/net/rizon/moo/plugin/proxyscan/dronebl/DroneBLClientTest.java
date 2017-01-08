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

import com.google.inject.Inject;
import com.google.inject.testing.fieldbinder.Bind;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.rizon.moo.plugin.proxyscan.conf.DroneBL;
import net.rizon.moo.plugin.proxyscan.conf.ProxyscanConfiguration;
import net.rizon.moo.test.MooJUnitRunner;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;

@RunWith(MooJUnitRunner.class)
public class DroneBLClientTest
{
	@Bind
	@Mock
	private ProxyscanConfiguration config;

	@Inject
	private DroneBLClient client;

	@Bind
	private EventLoopGroup group = new NioEventLoopGroup();

	@Test
	@Ignore
	public void testSubmit() throws InterruptedException
	{
		DroneBL dronebl = new DroneBL();
		dronebl.setDroneblHost("192.168.1.2");
		dronebl.setRpcKey("123");

		when(config.getDronebl()).thenReturn(dronebl);
		
		BlacklistedIP bl = new BlacklistedIP("1.2.3.4", 80, 3, "moo cow");
		client.submit(bl);

		Thread.sleep(10000L);
	}

}
