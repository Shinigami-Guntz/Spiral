package org.abimon.spiral.core.formats.scripting

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.UnsafeLin
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.DR1
import org.abimon.spiral.core.objects.game.hpa.DR2
import org.abimon.spiral.core.objects.game.hpa.HopesPeakDRGame
import org.abimon.spiral.core.objects.scripting.Lin
import org.abimon.spiral.core.objects.scripting.lin.LinTextScript
import org.abimon.spiral.core.objects.scripting.lin.StopScriptEntry
import org.abimon.spiral.core.objects.scripting.lin.TextCountEntry
import org.abimon.spiral.core.println
import java.io.InputStream
import java.io.OutputStream

object LINFormat : SpiralFormat {
    override val name = "LIN"
    override val extension = "lin"
    override val conversions: Array<SpiralFormat> = arrayOf(OpenSpiralLanguageFormat)

    override fun isFormatWithConfidence(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Pair<Boolean, Double> {
        try {
            val lin = Lin(game as? HopesPeakDRGame ?: return false to 1.0, dataSource) ?: return false to 1.0

            if (lin.entries.isEmpty())
                return false to 1.0
            when (lin.game) {
                DR1 -> if (lin.entries.first() is TextCountEntry && lin.entries.last() is StopScriptEntry) return true to 1.0
                DR2 -> if (lin.entries.first() is TextCountEntry && lin.entries.last() is StopScriptEntry) return true to 1.0
            }

            return true to 0.75
        } catch (illegal: IllegalArgumentException) {
        } catch (negative: NegativeArraySizeException) {
        }
        return false to 1.0
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params)) return true

        val translateNames = params["lin:translateNames"]?.toString()?.toBoolean() ?: true

        val hpaGame = game as? HopesPeakDRGame ?: return false

        output.println("OSL Script")
        output.println("Set Game To ${hpaGame.names[0]}")

        val lin = UnsafeLin(hpaGame, dataSource)

        lin.entries.forEach { entry ->
            if (translateNames) {
                if (entry is LinTextScript)
                    output.println("${game.opCodes[entry.opCode]?.first?.firstOrNull()
                            ?: "0x${entry.opCode.toString(16)}"}|${entry.text?.replace("\n", "\\n") ?: "Hello, Null!"}")
                else
                    output.println("${game.opCodes[entry.opCode]?.first?.firstOrNull()
                            ?: "0x${entry.opCode.toString(16)}"}|${entry.rawArguments.joinToString()}")
            } else {
                if (entry is LinTextScript)
                    output.println("0x${entry.opCode.toString(16)}|${entry.text?.replace("\n", "\\n")
                            ?: "Hello, Null!"}")
                else
                    output.println("0x${entry.opCode.toString(16)}|${entry.rawArguments.joinToString()}")
            }
        }

        return true
    }
}