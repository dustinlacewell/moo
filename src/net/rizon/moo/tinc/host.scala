package net.rizon.moo.tinc

import scala.collection.mutable.LinkedList

class host
{
	var name = ""
	var address = ""
	var port = 0
	var subnets: LinkedList[String] = LinkedList()
	var key = ""
		
	override def equals(other: Any): Boolean =
	{
		if (!other.isInstanceOf[host])
			return false
		
		var h: host = other.asInstanceOf[host]
			
		if (!this.name.equals(h.name))
			return false
		if (!this.address.equals(h.address))
			return false
		if (this.port != h.port)
			return false
		if (this.subnets.length != h.subnets.length)
			return false
		if (!this.subnets.exists(a => h.subnets.exists(b => a.equals(b))))
			return false
		if (!this.key.equals(h.key))
			return false
				
		return true
	}

	/* node is the node this host is on */
	def isUpToDate(no: node): Boolean =
	{
		var n = node.getNodeWithConfigName(no.getLayer(), name)
		if (n == null) // no node with this configured name (in tinc.conf) exists
			return false
		var other: host = n.getHost(name)
		if (other == null) // target node has no host for itself
			return false
		return this.equals(other)
	}
	
	def hasSelfConfiguration(no: node): Boolean =
	{
		var n = node.getNodeWithConfigName(no.getLayer(), name)
		if (n == null)
			return false
		var other: host = n.getHost(name)
		if (other == null)
			return false
		return true
	}
	
	def hasKnownHost(no: node): Boolean =
	{
		var n = node.getNodeWithConfigName(no.getLayer(), name)
		if (n == null)
			return false
		return true
	}
}