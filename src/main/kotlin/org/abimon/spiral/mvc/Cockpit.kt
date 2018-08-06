package org.abimon.spiral.mvc

import org.abimon.imperator.impl.InstanceOrder
import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.GurrenArgs
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.fonts.V3SPCFont
import org.abimon.spiral.core.formats.images.SRDFormat
import org.abimon.spiral.core.utils.DataHandler
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.modding.PluginManager
import org.abimon.spiral.mvc.gurren.Gurren

fun main(args: Array<String>) = startupSpiral(parseArgs(args))

fun startupSpiral(args: GurrenArgs) {
    DataHandler.byteArrayToMap = { byteArray -> SpiralData.MAPPER.readValue(byteArray, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.stringToMap = { string -> SpiralData.MAPPER.readValue(string, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.fileToMap = { file -> SpiralData.MAPPER.readValue(file, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.streamToMap = { stream -> SpiralData.MAPPER.readValue(stream, Map::class.java).mapKeys { (key) -> key.toString() } }

    if(SpiralModel.purgeCache)
        CacheHandler.purge()

    //Register Commands
    SpiralModel.imperator.hireSoldiers(Gurren)

    ModManager.scanForMods()
    PluginManager.scanForPlugins()

    SRDFormat.hook()
    V3SPCFont.hook()

    println("Initialising SPIRAL v${SpiralData.version ?: "Developer"}")

    if (!args.disableUpdateCheck) {
        //
    }

    while (true) {
        try {
            print(SpiralModel.scope.first)
            val unknown = SpiralModel.imperator.dispatch(InstanceOrder("STDIN", scout = null, data = readLine()
                    ?: break)).isEmpty()
            Thread.sleep(250)
            if (unknown)
                println("Unknown command")
        } catch (th: Throwable) {
            th.printStackTrace()
        }
    }

    if(SpiralModel.purgeCache)
        CacheHandler.purge() //Just in case shutdown hook doesn't go off
}

fun parseArgs(args: Array<String>): GurrenArgs {
    val disableUpdateCheck = args.any { str -> str.equals("--disable-update-check", true) || str.equals("--disable_update_check", true) }

    return GurrenArgs(
            disableUpdateCheck
    )
}