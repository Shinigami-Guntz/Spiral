package org.abimon.spiral.core.formats.video

import org.abimon.spiral.core.byteArrayOfInts
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.util.MediaWrapper
import org.abimon.visi.io.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*

object IVFFormat : SpiralFormat {
    override val name: String = "IVF"
    override val extension: String? = "ivf"
    override val conversions: Array<SpiralFormat> = arrayOf(MP4Format)

    val initialHeader = byteArrayOfInts(0x44, 0x4B, 0x49, 0x46)
    val secondHeader = byteArrayOfInts(0x56, 0x50, 0x38, 0x30)

    override fun isFormat(source: DataSource): Boolean = source.use { stream ->
        if (!Arrays.equals(stream.readPartialBytes(4), initialHeader))
            return@use false
        stream.skipBytes(4)

        return@use Arrays.equals(stream.readPartialBytes(4), secondHeader)
    }

    override fun convert(format: SpiralFormat, source: DataSource, output: OutputStream, params: Map<String, Any?>): Boolean {
        if(super.convert(format, source, output, params)) return true

        if (!MediaWrapper.ffmpeg.isInstalled) {
            errPrintln("ffmpeg is not installed, and thus we cannot convert from an IVF file to a ${format.name} file")
            return false
        }

        val tmpIn = File("${UUID.randomUUID()}.${extension}")
        val tmpOut = File("${UUID.randomUUID()}.${format.extension ?: "mp4"}") //unk won't be a valid conversion, so if all else fails let's be useful

        try {
            FileOutputStream(tmpIn).use { outputStream -> source.use { inputStream -> inputStream.writeTo(outputStream) } }

            MediaWrapper.ffmpeg.convert(tmpIn, tmpOut)

            FileInputStream(tmpOut).use { inputStream -> inputStream.writeTo(output) }
        } finally {
            tmpIn.delete()
            tmpOut.delete()
        }

        return true
    }
}