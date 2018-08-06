package org.abimon.spiral.mvc.gurren.imperator

import org.abimon.imperator.handle.Order
import org.abimon.imperator.handle.Soldier
import org.abimon.imperator.handle.Watchtower
import org.abimon.imperator.impl.InstanceOrder
import org.parboiled.Rule
import org.parboiled.parserunners.ReportingParseRunner

open class ParboiledSoldier(val rule: Rule, val scope: String? = null, private val watchtowers: Collection<Watchtower>, val command: (List<Any>) -> Unit) : Soldier {
    constructor(rule: Rule, scope: String? = null, command: (List<Any>) -> Unit) : this(rule, scope, java.util.Collections.singletonList(ParboiledWatchtower(rule, scope)), command)

    override fun command(order: Order) {
        when (order) {
            is InstanceOrder<*> -> {
                val command = order.data as? String ?: return

                val runner = ReportingParseRunner<Any>(rule)
                val result = runner.run(command)

                if (result.hasErrors())
                    return

                command(result.valueStack.reversed())
            }
        }
    }

    override fun getName(): String = "Parboiled Rule $rule"

    override fun getWatchtowers(): Collection<Watchtower> = watchtowers
}