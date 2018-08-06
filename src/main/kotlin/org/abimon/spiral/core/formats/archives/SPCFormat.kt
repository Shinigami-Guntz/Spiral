package org.abimon.spiral.core.formats.archives

import org.abimon.spiral.core.SpiralFormats
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.formats.models.ColladaModelFormat
import org.abimon.spiral.core.formats.models.OBJModelFormat
import org.abimon.spiral.core.objects.archives.SPC
import org.abimon.spiral.core.objects.archives.SRD
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.models.SRDIModel
import org.abimon.spiral.core.objects.models.collada.ColladaPojo
import org.abimon.spiral.core.utils.*
import org.abimon.spiral.util.decompress
import org.abimon.visi.lang.replaceLast
import java.io.InputStream
import java.io.OutputStream
import java.io.PrintStream
import java.text.DecimalFormat
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object SPCFormat : SpiralFormat {
    override val name = "SPC"
    override val extension = "spc"
    override val conversions: Array<SpiralFormat> = arrayOf(ZIPFormat, OBJModelFormat)
    val decimalFormat = DecimalFormat("#.####")

    override fun isFormat(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Boolean {
        try {
            return SPC(dataSource).files.isNotEmpty()
        } catch (e: IllegalArgumentException) {
        }
        return false
    }

    override fun convert(game: DRGame?, format: SpiralFormat, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream, output: OutputStream, params: Map<String, Any?>): Boolean {
        if (super.convert(game, format, name, context, dataSource, output, params)) return true

        val spc = SPC(dataSource)
        val convert = "${params["spc:convert"] ?: false}".toBoolean()
        when (format) {
            is ZIPFormat -> {
                val zip = ZipOutputStream(output)
                spc.files.forEach { entry ->
                    val data = decompress(entry::inputStream)
                    if (convert) {
                        val innerFormat = SpiralFormats.formatForData(game, data, "$name/${entry.name}", SpiralFormats.drArchiveFormats)
                        val convertTo = innerFormat?.conversions?.firstOrNull()

                        if (innerFormat != null && convertTo != null) {
                            zip.putNextEntry(ZipEntry(entry.name.replaceLast(".${innerFormat.extension}", "") + ".${convertTo.extension
                                    ?: "unk"}"))
                            innerFormat.convert(game, convertTo, "$name/${entry.name}", context, data, zip, params)
                            return@forEach
                        } else if (innerFormat != null) {
                            zip.putNextEntry(ZipEntry(entry.name.replaceLast(".${innerFormat.extension}", "") + ".${innerFormat.extension}"))
                            data().use { stream -> stream.copyTo(zip) }
                            return@forEach
                        }
                    }

                    zip.putNextEntry(ZipEntry(entry.name))
                    data().use { stream -> stream.copyTo(zip) }
                }
                zip.finish()
            }
            is OBJModelFormat -> {
                val baseNames = spc.files.map { entry -> entry.name.substringBeforeLast('.') }.distinct()
                val modelName = baseNames.firstOrNull { rootName -> spc.files.any { entry -> entry.name == "$rootName.srd" } && spc.files.any { entry -> entry.name == "$rootName.srdi" } }
                        ?: return false

                val srd = SRD(spc.files.first { entry -> entry.name == "$modelName.srd" }::inputStream)
                val srdi = SRDIModel(srd, spc.files.first { entry -> entry.name == "$modelName.srdi" }::inputStream)

                val flipUVs = "${params["srdi:flipUVs"] ?: true}".toBoolean()
                val invertXAxis = "${params["srdi:invertX"] ?: true}".toBoolean()

                val out = PrintStream(output)

                out.println("# SPIRAL v${SpiralData.version ?: "Developer"}")
                out.println("# Autogenerated")
                out.println()

                var offset = 0

                srdi.meshes.forEachIndexed { index, mesh ->
                    out.println("g ${mesh.name}")
                    out.println("# ${mesh::class.simpleName}")

                    val vertices: List<Vertex> = if (invertXAxis) mesh.vertices.map { (x, y, z) -> Vertex(x * -1, y, z) } else mesh.vertices.toList()
                    val uvs: List<UV> = if (flipUVs) mesh.uvs.map { (u, v) -> UV(u, 1.0f - v) } else mesh.uvs.toList()
                    val normals: List<Vertex> = if (invertXAxis) mesh.normals?.map { (x, y, z) -> Vertex(x * -1, y, z) }
                            ?: emptyList() else mesh.normals?.toList() ?: emptyList()

                    vertices.map(decimalFormat::formatTriple).forEach { (x, y, z) -> out.println("v $x $y $z") }
                    uvs.map(decimalFormat::formatPair).forEach { (u, v) -> out.println("vt $u $v") }
                    normals.map(decimalFormat::formatTriple).forEach { (x, y, z) -> out.println("vn $x $y $z") }

                    mesh.faces.map { (a, b, c) -> TriFace(c, b, a) }.forEach { (a, b, c) ->
                        if (a in uvs.indices && b in uvs.indices && c in uvs.indices) {
                            if (a in normals.indices && b in normals.indices && c in normals.indices) {
                                out.println("f ${a + 1 + offset}/${a + 1 + offset}/${a + 1 + offset} ${b + 1 + offset}/${b + 1 + offset}/${b + 1 + offset} ${c + 1 + offset}/${c + 1 + offset}/${c + 1 + offset}")
                            } else {
                                out.println("f ${a + 1 + offset}/${a + 1 + offset} ${b + 1 + offset}/${b + 1 + offset} ${c + 1 + offset}/${c + 1 + offset}")
                            }
                        } else {
                            out.println("f ${a + 1 + offset} ${b + 1 + offset} ${c + 1 + offset}")
                        }
                    }

                    offset += mesh.vertices.size

                    out.println()
                }
            }

            ColladaModelFormat -> {
                val baseNames = spc.files.map { entry -> entry.name.substringBeforeLast('.') }.distinct()
                val modelName = baseNames.firstOrNull { rootName -> spc.files.any { entry -> entry.name == "$rootName.srd" } && spc.files.any { entry -> entry.name == "$rootName.srdi" } }
                        ?: return false

                val srd = SRD(spc.files.first { entry -> entry.name == "$modelName.srd" }::inputStream)
                val srdi = SRDIModel(srd, spc.files.first { entry -> entry.name == "$modelName.srdi" }::inputStream)

                val flipUVs = "${params["srdi:flipUVs"] ?: true}".toBoolean()
                val invertXAxis = "${params["srdi:invertX"] ?: true}".toBoolean()

                SpiralData.XML_MAPPER.writeValue(output, ColladaPojo(srdi, flipUVs, invertXAxis, name))
            }
        }

        return true
    }
}