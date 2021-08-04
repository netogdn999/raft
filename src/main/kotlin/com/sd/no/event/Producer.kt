package com.sd.no.event

import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class Producer (
        private val kafkaTemplate: KafkaTemplate<String, String>,
) {

    private val TOPICO_REQUEST_VOTES = "requestVotes"
    private val TOPICO_APPEND_ENTRIES = "appendEntries"

    fun requestVotes() {
        if(Consumer.election[Consumer.term] == null) {
            Consumer.term++
            Consumer.timeoutElection = Random(System.currentTimeMillis()).nextLong(from = 150, until = 300)
            Consumer.starTime = System.currentTimeMillis()
            if(!Consumer.job1.isAlive)
                Consumer.job1.start()
            kafkaTemplate.send(TOPICO_REQUEST_VOTES, "")
        }
    }

    fun appendEntries() {
        // todo informa as seguidores que é o lider
        kafkaTemplate.send(TOPICO_APPEND_ENTRIES, "")
    }
}