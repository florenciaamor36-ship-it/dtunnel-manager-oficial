package com.example.service

/** Expands the payload dialect used by profiles without contacting any endpoint. */
object PayloadParser {
    data class Expanded(val bytes: List<ByteArray>, val host: String?, val port: Int?)

    fun expand(template: String, host: String, port: Int): Expanded {
        require(host.isNotBlank()) { "El host está vacío" }
        require(port in 1..65535) { "Puerto inválido" }
        val alternatives = template.split("[rotate=").flatMap { part ->
            if (part.contains("]")) {
                val values = part.substringBefore("]").split(';').filter { it.isNotBlank() }
                values.map { template.replace(Regex("\\[rotate=[^]]+\\]"), it) }
            } else listOf(template)
        }.ifEmpty { listOf(template) }
        val rendered = alternatives.map { item ->
            item.replace("[host_port]", "$host:$port")
                .replace("[crlf]", "\r\n")
                .replace("[lf]", "\n")
                .replace("[split]", "")
        }
        return Expanded(rendered.map { it.toByteArray(Charsets.ISO_8859_1) }, host, port)
    }
}
