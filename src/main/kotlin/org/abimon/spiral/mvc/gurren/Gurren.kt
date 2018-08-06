package org.abimon.spiral.mvc.gurren

import org.abimon.spiral.mvc.gurren.imperator.CommandClass
import org.abimon.spiral.mvc.gurren.imperator.ParboiledSoldier

object Gurren : CommandClass {
    val helpRule = makeRule { IgnoreCase("help") }

    val help = ParboiledSoldier(helpRule) { }
}