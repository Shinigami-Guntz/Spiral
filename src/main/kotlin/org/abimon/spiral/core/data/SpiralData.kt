package org.abimon.spiral.core.data

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.Nulls
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import com.github.kittinunf.fuel.Fuel
import org.abimon.imperator.handle.Imperator
import org.abimon.spiral.core.objects.models.SRDIMesh
import org.abimon.spiral.core.userAgent
import org.abimon.spiral.core.utils.TriFace
import org.abimon.spiral.core.utils.Vertex
import org.abimon.spiral.modding.IPlugin
import org.abimon.spiral.mvc.SpiralModel
import org.slf4j.LoggerFactory
import java.io.File
import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import java.security.MessageDigest

object SpiralData {
    const val billingDead = true
    const val JENKINS_PATH = "https://jenkins.abimon.org"
    const val JENKINS_PROJECT_NAME = "SPIRAL-MVC"
    const val JENKINS_PROJECT_FILE = "$JENKINS_PROJECT_NAME-all.jar"

    val nonstopOpCodes = hashMapOf(
            0x00 to "TextID",
            0x01 to "Type",
            0x03 to "Shoot With Evidence",
            0x06 to "Has Weak Point",
            0x07 to "Advance",
            0x0A to "Transition",
            0x0B to "Fadeout",
            0x0C to "Horizontal",
            0x0D to "Vertical",
            0x0E to "Angle Acceleration",
            0x0F to "Angle",
            0x10 to "Scale",
            0x11 to "Final Scale",
            0x13 to "Rotation",
            0x14 to "Rotation Speed",
            0x15 to "Character",
            0x16 to "Sprite",
            0x17 to "Background Animation",
            0x19 to "Voice",
            0x1B to "Chapter"
    )

    val MAPPER: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    val YAML_MAPPER: ObjectMapper = ObjectMapper(YAMLFactory())
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)

    val XML_MAPPER: ObjectMapper = XmlMapper(JacksonXmlModule().apply { setDefaultUseWrapper(false) })
            .registerKotlinModule()
            .registerModules(Jdk8Module(), JavaTimeModule(), ParameterNamesModule(), InstantSerialisation.MODULE())
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
            .setSerializationInclusion(JsonInclude.Include.NON_ABSENT)
            .setDefaultSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY))

    val version: String? by lazy {
        val file = File(SpiralModel::class.java.protectionDomain.codeSource.location.path)
        if (!file.isFile)
            return@lazy null

        val md = MessageDigest.getInstance("MD5")

        val channel = FileChannel.open(file.toPath(), StandardOpenOption.READ)
        val buffer = ByteBuffer.allocate(8192)

        while (channel.isOpen) {
            val read = channel.read(buffer)
            if (read <= 0)
                break


            buffer.flip()
            md.update(buffer)
            buffer.rewind()
        }

        return@lazy String.format("%032x", BigInteger(1, md.digest()))
    }
    val build: Int? by lazy {
        version?.let { hash -> SpiralData.jenkinsBuildFor(hash).first }
    }

    val LOGGER = LoggerFactory.getLogger("Spiral v${version ?: "Developer"}")

    val STEAM_DANGANRONPA_TRIGGER_HAPPY_HAVOC = "413410"
    val STEAM_DANGANRONPA_2_GOODBYE_DESPAIR = "413420"
    val SPIRAL_HEADER_NAME = "Spiral-Header"
    val SPIRAL_PRIORITY_LIST = "Spiral-Priority-List"
    val SPIRAL_MOD_LIST = "Spiral-Mod-List"

    val BASE_PLUGIN = object : IPlugin {
        override fun enable(imperator: Imperator) {}
        override fun disable(imperator: Imperator) {}
    }

    val cube = SRDIMesh(
            arrayOf(Vertex(1f, 1f, -1f), Vertex(1f, -1f, -1f), Vertex(-1f, -1f, -1f), Vertex(-1f, 1f, -1f), Vertex(1f, 1f, 1f), Vertex(1f, -1f, 1f), Vertex(-1f, -1f, 1f), Vertex(-1f, 1f, 1f)),
            emptyArray(),
            arrayOf(TriFace(0, 2, 3), TriFace(7, 5, 4), TriFace(4, 1, 0), TriFace(5, 2, 1), TriFace(2, 7, 3), TriFace(0, 7, 4), TriFace(0, 1, 2), TriFace(7, 6, 5), TriFace(4, 5, 1), TriFace(5, 6, 2), TriFace(2, 6, 7), TriFace(0, 3, 7))
    )

    fun jenkinsBuildFor(hash: String): Pair<Int?, Int> {
        val (_, response) = Fuel.get("${SpiralData.JENKINS_PATH}/fingerprint/$hash/api/json").userAgent().responseString()

        if (response.statusCode != 200 && response.statusCode != 404)
            LOGGER.warn("Jenkins returned ${response.statusCode} for getting the hash of $hash (Jenkins Path: $JENKINS_PATH). Message: ${response.responseMessage}")

        val build: Int?

        if (response.statusCode != 200) {
            build = null
        } else {
            build = ((SpiralData.MAPPER.readValue(response.data, Map::class.java)["original"] as? Map<*, *>
                    ?: emptyMap<String, String>())["number"] as? Int)
        }

        return build to response.statusCode
    }
}