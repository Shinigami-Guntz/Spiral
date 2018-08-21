package org.abimon.spiral.mvc.gurren


import com.jakewharton.fliptables.FlipTable
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.gurren.imperator.CommandClass
import org.abimon.spiral.mvc.gurren.imperator.ParboiledSoldier
import org.abimon.spiral.util.decompressWithFormats
import org.abimon.visi.io.errPrintln
import java.io.File

@Suppress("unused")
object Gurren : CommandClass {
    /** Helper Variables */
    var keepLooping = true

    /** Rules */
    val helpRule = makeRule { IgnoreCase("help") }

    val identifyRule = makeRule {
        Sequence(
                IgnoreCase("identify"),
                InlineWhitespace(),
                FilePath()
        )
    }

    /** Commands */

    val help = ParboiledSoldier(helpRule) { }

    val identify = ParboiledSoldier(identifyRule) { stack ->
        val file = stack[0] as File

        // First thing's first - does the file even exist?
        if (!file.exists())
            return@ParboiledSoldier errPrintln("Error: $file does not exist")

        //Next up, are we dealing with a singular file?
        if (file.isFile) {
            //If so, we can define a data source for it here
            //We decompress it in place, just in case it's compressed
            val (dataSource, compressionMethods) = decompressWithFormats(file::inputStream)

            //We should now have a proper data source
            //We can now work on format identification
            val format = SpiralFormats.formatForData(null, dataSource, file.name)

            if (format != null) {
                //The file has an identifiable format.

                //Should result in something like DRVita > V3 > SPC >
                val compressionString = if (compressionMethods.isEmpty()) "" else compressionMethods.joinToString(" > ", postfix = " > ")

                //This concatenates them together, which will be something like DRVita > V3 > SPC > SRD, or just SRD if it's uncompressed
                val formatString = "${compressionString}${format.name}"

                //Print it all out
                if (SpiralModel.tableOutput) {
                    println(FlipTable.of(arrayOf("File", "Format"), arrayOf(arrayOf(file.absolutePath, formatString))))
                } else {
                    println("Identified ${file.absolutePath}")
                    println("Format: $formatString")
                }
            }
        }
    }
}