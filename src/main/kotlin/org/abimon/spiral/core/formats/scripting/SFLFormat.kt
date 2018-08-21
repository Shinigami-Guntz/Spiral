package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.readString
import java.io.InputStream

object SFLFormat: SpiralFormat {
    override val name: String = "SFL"
    override val extension: String? = "sfl"
    override val conversions: Array<SpiralFormat> = emptyArray()

    override fun isFormatWithConfidence(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Pair<Boolean, Double> = dataSource().use { stream -> stream.readString(4) == "LLFS" } to 1.0
}