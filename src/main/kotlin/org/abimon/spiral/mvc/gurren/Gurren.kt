package org.abimon.spiral.mvc.gurren

import com.mitchtalmadge.asciidata.table.ASCIITable
import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.mvc.SpiralModel
import org.abimon.spiral.mvc.gurren.imperator.CommandClass
import org.abimon.spiral.mvc.gurren.imperator.GurrenCommand
import org.abimon.spiral.mvc.gurren.imperator.ParboiledSoldier
import org.abimon.spiral.util.addLinebreaks
import org.abimon.spiral.util.decompressWithFormats
import org.abimon.visi.io.errPrintln
import org.abimon.visi.lang.times
import org.jline.terminal.TerminalBuilder
import java.io.File
import kotlin.math.max
import kotlin.math.min

@Suppress("unused")
object Gurren : CommandClass {
    /** Helper Variables */
    var keepLooping = true
    val terminal = TerminalBuilder.terminal()
    val lineWidth: Int
        get() {
            var width = terminal.width
            if (width < 10)
                width = 80

            return width - 5
        }

    /** Rules */
    val helpRule = makeRule { IgnoreCase("help") }

    val identifyRule = makeRule {
        Sequence(
                IgnoreCase("identify"),
                InlineWhitespace(),
                FilePath()
        )
    }

    val convertRule = makeRule {
        Sequence(
                IgnoreCase("convert"),
                InlineWhitespace(),
                FilePath()
        )
    }

    /** Help Output */

    val helpHelp = arrayOf("Help", "Display this prompt", "help")
    val identifyHelp = arrayOf("Identify", "Identify a file's format", "identify [file path]" * 10)

    /** Commands */

    val help = ParboiledSoldier(helpRule, helpOutput = helpHelp) {
        if (SpiralModel.tableOutput) {
            //We filter all the registered commands to instances of GurrenCommand
            val commands = SpiralModel.imperator.soldiers.filterIsInstance(GurrenCommand::class.java)

            //Then, we map to the help output of that command, and sort by the name
            val commandOutput = commands.map(GurrenCommand::helpOutput)
                    .map(Array<String>::copyOf)
                    .filter { helpOutput -> helpOutput[0].isNotBlank() }
                    .sortedBy { (commandName) -> commandName }
                    .toTypedArray()
                    .addLinebreaks(lineWidth / 3, 0, 2, 1)

            println(ASCIITable.fromData(arrayOf("Command Name", "Command Desc", "Command Syntax"), commandOutput))
        }
    }

    val identify = ParboiledSoldier(identifyRule, helpOutput = identifyHelp) { stack ->
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
                    println(ASCIITable.fromData(arrayOf("File", "Format"), arrayOf(arrayOf(file.absolutePath, formatString)).addLinebreaks(lineWidth / 2, 0, 1)))
                } else {
                    println("Identified ${file.absolutePath}")
                    println("Format: $formatString")
                }
            }
        }
    }

    val convert = ParboiledSoldier(convertRule) { stack ->

    }
}