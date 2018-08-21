package org.abimon.spiral.core.data

import org.abimon.spiral.core.GurrenOutput
import org.abimon.spiral.core.GurrenOutputFormat

object GurrenOutputFormats {
    val stdout: GurrenOutputFormat = this::stdout

    @JvmStatic
    fun stdout(output: GurrenOutput) {
        val (header, data) = output

        println(header.joinToString("|"))
        println(data.joinToString("\n") { line -> line.joinToString("|") })
    }
}