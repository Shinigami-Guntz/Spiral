package org.abimon.spiral.mvc.gurren.imperator

import org.abimon.spiral.mvc.SpiralModel
import org.parboiled.Rule

interface CommandClass {
    val parser: ImperatorParser
        get() = SpiralModel.imperatorParser

    fun <T : Rule> makeRule(op: ImperatorParser.() -> T): T {
        return parser.op()
    }
}