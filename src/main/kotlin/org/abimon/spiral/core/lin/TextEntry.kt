package org.abimon.spiral.core.lin

data class TextEntry(val text: String, val textID: Int, val starting: Int, val ending: Int): LinScript {
    constructor(text: String): this(text, 0, 0, 0)
    override fun getOpCode(): Int = 0x02

    override fun getRawArguments(): IntArray = intArrayOf(starting, ending)
}