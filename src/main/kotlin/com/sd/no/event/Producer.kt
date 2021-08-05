package com.sd.no.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sd.no.dto.LogDTO
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class Producer (
        private val kafkaTemplate: KafkaTemplate<String, String>
) {

    private val TOPICO_REQUEST_VOTES = "requestVotes"
    private val TOPICO_APPEND_ENTRIES = "appendEntries"

    fun requestVotes() {
        val logDTO = LogDTO (
            "12333",
            12
        )
        val json = jacksonObjectMapper().writeValueAsString(logDTO)
        kafkaTemplate.send(TOPICO_REQUEST_VOTES, json)
    }

    fun appendEntries() {
        // todo informa as seguidores que é o lider
        kafkaTemplate.send(TOPICO_APPEND_ENTRIES, "")
    }
}