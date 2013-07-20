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
}