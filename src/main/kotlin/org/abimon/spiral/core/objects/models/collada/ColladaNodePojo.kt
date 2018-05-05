package org.abimon.spiral.core.objects.models.collada

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

@JsonRootName("node")
data class ColladaNodePojo(
        @JacksonXmlProperty(isAttribute = true)
        val id: String?,
        @JacksonXmlProperty(isAttribute = true)
        val name: String?,
        @JacksonXmlProperty(isAttribute = true)
        val sid: String?,
        @JacksonXmlProperty(isAttribute = true)
        val type: String?,
        @JacksonXmlProperty(isAttribute = true)
        val layer: String?,

        val instance_geometry: List<ColladaInstanceGeometryPojo> = emptyList()
)