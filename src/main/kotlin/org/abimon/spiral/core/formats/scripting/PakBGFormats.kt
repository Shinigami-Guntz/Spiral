package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import java.io.InputStream

object PakBGFormats: SpiralFormat {
    override val name: String = "Pak BGs"
    override val extension: String? = null
    override val conversions: Array<SpiralFormat> = emptyArray()
    private val NAME_REGEX = "bg_.+_(file|opt|place)\\.dat".toRegex()

    override fun isFormatWithConfidence(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Pair<Boolean, Double> = (name?.matches(NAME_REGEX) == true) to 0.4
}