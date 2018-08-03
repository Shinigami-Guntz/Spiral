package org.abimon.spiral.mvc

import org.abimon.spiral.core.data.CacheHandler
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.fonts.V3SPCFont
import org.abimon.spiral.core.formats.images.SRDFormat
import org.abimon.spiral.core.utils.DataHandler
import org.abimon.spiral.modding.ModManager
import org.abimon.spiral.modding.PluginManager

fun main(args: Array<String>) = startupSpiral(args)

fun startupSpiral(args: Array<String>) {
    DataHandler.byteArrayToMap = { byteArray -> SpiralData.MAPPER.readValue(byteArray, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.stringToMap = { string -> SpiralData.MAPPER.readValue(string, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.fileToMap = { file -> SpiralData.MAPPER.readValue(file, Map::class.java).mapKeys { (key) -> key.toString() } }
    DataHandler.streamToMap = { stream -> SpiralData.MAPPER.readValue(stream, Map::class.java).mapKeys { (key) -> key.toString() } }

    if(SpiralModel.purgeCache)
        CacheHandler.purge()

    ModManager.scanForMods()
    PluginManager.scanForPlugins()

    SRDFormat.hook()
    V3SPCFont.hook()

    println("Initialising SPIRAL v${SpiralData.version ?: "Developer"}")

    if(SpiralModel.purgeCache)
        CacheHandler.purge() //Just in case shutdown hook doesn't go off
}