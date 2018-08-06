package org.abimon.spiral.mvc.gurren.imperator

import org.abimon.imperator.handle.Order
import org.abimon.imperator.handle.Watchtower
import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.mvc.SpiralModel
import org.parboiled.Rule
import org.parboiled.parserunners.ReportingParseRunner

open class ParboiledWatchtower(val rule: Rule, val scope: String? = null) : Watchtower {
    val runner = ReportingParseRunner<Any>(rule)

    override fun allow(order: Order): Boolean {
        if (scope != null && SpiralModel.scope.second != scope)
            return false

        when (order) {
            is InstanceOrder<*> -> {
                val command = order.data as? String ?: return false

                return !runner.run(command).hasErrors()
            }
            else -> return false
        }
    }

    override fun getName(): String = "Parboiled Watchtower"
}