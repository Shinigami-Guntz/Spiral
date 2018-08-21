package org.abimon.spiral.core.formats.models

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import org.abimon.spiral.core.data.SpiralData
import org.abimon.spiral.core.formats.SpiralFormat
import org.abimon.spiral.core.objects.game.DRGame
import org.abimon.spiral.core.objects.models.collada.ColladaPojo
import java.io.CharConversionException
import java.io.InputStream

object ColladaModelFormat : SpiralFormat {
    override val name: String = "Collada"
    override val extension: String? = "dae"
    override val conversions: Array<SpiralFormat> = emptyArray()

    override fun isFormatWithConfidence(game: DRGame?, name: String?, context: (String) -> (() -> InputStream)?, dataSource: () -> InputStream): Pair<Boolean, Double> {
        try {
            dataSource().use { stream -> SpiralData.XML_MAPPER.readValue(stream, ColladaPojo::class.java) }
            return true to 1.0 //This could be a bit better honestly, but it'll do
        } catch (json: JsonParseException) {
        } catch (json: JsonMappingException) {
        } catch (io: CharConversionException) {
        }

        return false to 1.0
    }
}