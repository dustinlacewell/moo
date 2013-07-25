package net.rizon.moo.servermonitor

import net.rizon.moo._

import java.util.Date
import java.util.Calendar
import java.util.HashSet
import java.security.cert._

object certChecker
{
	var sslPort = 6697
	var warned = new HashSet[String]
	var error = new HashSet[String]
	var lastIdx = -1
	
	private def report(what: String)
	{
		for (chan <- moo.conf.getAdminChannels())
			moo.reply(null, chan, "[CERTCHECKER] " + what)
	}
	
	def run()
	{
		var servers = server.getServers()
		lastIdx += 1
		if (lastIdx >= servers.length)
			lastIdx = 0
		for (lastIdx <- lastIdx to servers.length)
		{
			var server = servers(lastIdx)
			var ok = true
			
			if (!server.isHub() && !server.isServices())
			{
				if (server.cert == null)
				{
					var sc = new scheck(server, moo.conf.getAdminChannels(), true, certChecker.sslPort, true, false);
					sc.start
					return
				}
				else
				{
					try
					{
						var now = new Date()
						server.cert.checkValidity(now)
						
						try
						{
							var c = Calendar.getInstance()
							c.setTime(now)
							c.add(Calendar.DATE, 1)
							server.cert.checkValidity(c.getTime())
						}
						catch
						{
							case e: CertificateExpiredException =>
							{
								ok = false
								if (!certChecker.warned.contains(server.getName()) && !certChecker.error.contains(server.getName()))
								{
									report("SSL certificate for " + server.getName() + " expires on " + server.cert.getNotAfter() + ", which is " + moo.difference(now, server.cert.getNotAfter()) + " from now")
									certChecker.warned.add(server.getName())
								}
							}
						}
					}
					catch
					{
						case e: CertificateExpiredException =>
						{
							ok = false
							if (!certChecker.error.contains(server.getName()))
							{
								report(server.getName + " has EXPIRED X509 SSL certificate!");
								certChecker.error.add(server.getName())
							}
						}
						case e: CertificateNotYetValidException =>
						{
							ok = false
							if (!certChecker.error.contains(server.getName()))
							{
								report(server.getName + " has a NOT VALID YET X509 SSL certificate!");
								certChecker.error.add(server.getName())
							}
						}
					}
					
					if (ok)
					{
						certChecker.warned.remove(server.getName())
						certChecker.error.remove(server.getName())
					}
				}
			}
		}
	}
}