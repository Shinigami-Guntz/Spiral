package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.archives.CPK
import org.abimon.spiral.core.objects.game.DRGame
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object CPKFormat: SpiralFormat {
    override val name: String = "CPK"
    override val extension: String = "cpk"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat, WADFormat)

    override fun isFormatWithConfidence(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Pair<Boolean, Double> {
        try {
            val cpk = CPK(dataSource)

            if (cpk.files.size == 1)
                return true to 0.75
            return cpk.files.isNotEmpty() to 1.0
        } catch(iea: IllegalArgumentException) {
            return false to 1.0
        }
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(game, format, name, context, dataSource, output, params)) return true
        val cpk = CPK(dataSource)

        when(format) {
            ZIPFormat -> {
                val zip = ZipOutputStream(output)
                cpk.files.forEach { entry ->
                    zip.putNextEntry(ZipEntry("${entry.directoryName}/${entry.fileName}"))
                    entry.inputStream.use { stream -> stream.copyTo(zip) }

                    return@forEach
                }
                zip.finish()
            }
        }

        return true
    }
}