package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.UnsafeNonstopDebate
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.HopesPeakKillingGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.NonstopDebate
import org.abimon.spiral.core.println
import java.io.InputStream
import java.io.OutputStream

object NonstopFormat: SpiralFormat {
    override val name: String = "Nonstop Debate"
    override val extension: String = "dat"
    override val conversions: Array<SpiralFormat> = arrayOf(OpenSpiralLanguageFormat)

    override fun isFormatWithConfidence(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Pair<Boolean, Double> {
        val nonstop = NonstopDebate(game as? HopesPeakKillingGame
                ?: UnknownHopesPeakGame, dataSource) ?: return false to 1.0

        return nonstop.sections.isNotEmpty() to 0.5
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true

        val debate = UnsafeNonstopDebate(game as? HopesPeakKillingGame ?: UnknownHopesPeakGame, dataSource)

        when(format) {
            OpenSpiralLanguageFormat -> {
                val originalGame = debate.game

                output.println("OSL Script")
                output.println("Game Context: \"Nonstop Debate (${originalGame.names.firstOrNull()})\"")

                debate.sections.forEach { section ->
                    output.println("")
                    output.println("[New Section]")
                    for (i in 0 until originalGame.nonstopDebateSectionSize / 2)
                        output.println("${originalGame.nonstopDebateOpCodeNames[i]
                                ?: "0x${i.toString(16)}"}|${section[i]}")
                }
            }
        }

        return true
    }
}