package com.sd.no.event

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class Producer (
        //private val kafkaTemplate: KafkaTemplate<String, String>,
) {

    private val TOPICO_REQUEST_VOTES = "requestVotes"
    private val TOPICO_APPEND_ENTRIES = "appendEntries"

    fun requestVotes() {
        //kafkaTemplate.send(TOPICO_REQUEST_VOTES, "")
    }

    fun appendEntries() {
        //kafkaTemplate.send(TOPICO_APPEND_ENTRIES, "")
    }
}