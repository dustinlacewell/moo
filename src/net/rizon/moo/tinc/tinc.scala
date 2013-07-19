package net.rizon.moo.tinc

import net.rizon.moo.mpackage

object tinc
{
	def tincBase = ".tinc"
	var layers: List[layer] = List(new layer("tl"), new layer("rc"), new layer("rh"))
}

class tinc extends mpackage("tinc", "Manages tinc")
{
	new commandTinc(this)
}
