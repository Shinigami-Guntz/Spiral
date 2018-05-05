package org.abimon.spiral.core.formats.scripting

import org.abimon.osl.OpenSpiralLanguageParser
import org.abimon.osl.SpiralDrillBit
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.customLin
import org.abimon.spiral.core.objects.customWordScript
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.game.hpa.UnknownHopesPeakGame
import org.abimon.spiral.core.objects.scripting.lin.LinScript
import org.abimon.spiral.core.objects.scripting.wrd.WrdScript
import org.abimon.spiral.util.debug
import org.abimon.visi.lang.EnumOS
import java.io.InputStream
import java.io.OutputStream

object OpenSpiralLanguageFormat: SpiralFormat {
    override val name: String = "Open Spiral Language"
    override val extension: String = "osl"
    override val conversions: Array<SpiralFormat> = arrayOf(LINFormat, WRDFormat)

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        val text = String(dataSource().use { stream -> stream.readBytes() }, Charsets.UTF_8)

        val parser = OpenSpiralLanguageParser { fileName -> context(fileName)?.invoke()?.use { stream -> stream.readBytes() }}

        parser.game = game ?: UnknownHopesPeakGame
        parser["FILENAME"] = name
        parser["OS"] = EnumOS.determineOS().name

        val result = parser.parse(text)
        return !result.hasErrors() && !result.valueStack.isEmpty
    }

    @Suppress("UNCHECKED_CAST")
    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params)) return true

        val text = String(dataSource().use { stream -> stream.readBytes() }, Charsets.UTF_8)
        val parser = OpenSpiralLanguageParser { fileName -> context(fileName)?.invoke()?.use { stream -> stream.readBytes() }}
        val lang = context("en_US.lang")

        if (lang != null)
            parser.localiser = { unlocalised ->
                lang().use { stream -> String(stream.readBytes()) }.split('\n').firstOrNull { localised -> localised.startsWith("$unlocalised=") }?.substringAfter("$unlocalised=")?.replace("\\n", "\n")
                        ?: unlocalised
            }

        parser.game = game ?: UnknownHopesPeakGame
        parser["FILENAME"] = name
        parser["OS"] = EnumOS.determineOS().name

        val result = parser.parse(text)
        val stack = result.valueStack?.toList()?.asReversed() ?: return false

        if (result.hasErrors() || result.valueStack.isEmpty)
            return false

        when (format) {
            LINFormat -> {
                val customLin = customLin {
                    stack.forEach { value ->
                        debug("Stack Value: $value")

                        if (value is List<*>) {
                            val drillBit = (value[0] as? SpiralDrillBit) ?: return@forEach
                            val head = drillBit.head
                            try {
                                val valueParams = value.subList(1, value.size).filterNotNull().toTypedArray()

                                val products = head.operate(parser, valueParams)

                                when (head.klass) {
                                    LinScript::class -> add(products as LinScript)
                                    Array<LinScript>::class -> addAll(products as Array<LinScript>)
                                    Unit::class -> { }
                                    else -> System.err.println("${head.klass} not a recognised product type!")
                                }
                            } catch (th: Throwable) {
                                throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
                            }
                        }
                    }
                }

                if (customLin.entries.isEmpty())
                    return false

                customLin.compile(output)
            }
            WRDFormat -> {
                val customWordScript = customWordScript {
                    stack.forEach { value ->
                        debug("Stack Value: $value")
                        if (value is List<*>) {
                            val drillBit = (value[0] as? SpiralDrillBit) ?: return@forEach
                            val head = drillBit.head
                            try {
                                val valueParams = value.subList(1, value.size).filterNotNull().toTypedArray()

                                val products = head.operate(parser, valueParams)

                                when (head.klass) {
                                    WrdScript::class -> add(products as WrdScript)
                                    Array<WrdScript>::class -> addAll(products as Array<WrdScript>)
                                    Unit::class -> { }
                                    else -> System.err.println("${head.klass} not a recognised product type!")
                                }
                            } catch (th: Throwable) {
                                throw IllegalArgumentException("Script line [${drillBit.script}] threw an error", th)
                            }
                        }
                    }
                }

                if (customWordScript.entries.isEmpty())
                    return false

                customWordScript.compile(output)
            }
        }

        return true
    }
}