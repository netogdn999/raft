package com.sd.no.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sd.no.dto.AppendEntriesDTO
import com.sd.no.dto.ConfirmEntryDTO
import com.sd.no.dto.HeartBeatDTO
import com.sd.no.dto.SendVotesDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class Producer (
        private val kafkaTemplate: KafkaTemplate<String, String>
) {
    @Value("\${server.port}")
    var port: String = ""
    private val TOPIC_REQUEST_VOTES = "requestVotes"
    private val TOPIC_APPEND_ENTRIES = "appendEntries"
    private val TOPIC_HEART_BEATS = "heartBeats"
    private val TOPIC_CONFIRM_ENTRY = "confirmEntry"
    private val TOPIC_SEND_VOTE = "sendVote"

    fun requestVotes() =
        kafkaTemplate.send(TOPIC_REQUEST_VOTES, port)

    fun appendEntries(term: Int, leader: String) {
        val appendEntriesDTO = AppendEntriesDTO (
            term,
            leader
        )
        val json = jacksonObjectMapper().writeValueAsString(appendEntriesDTO)
        kafkaTemplate.send(TOPIC_APPEND_ENTRIES, json)
    }

    fun sendVotes(destination: String) {
        val sendVotesDTO = SendVotesDTO (
            port,
            destination
        )
        val json = jacksonObjectMapper().writeValueAsString(sendVotesDTO)
        kafkaTemplate.send(TOPIC_SEND_VOTE, json)
    }

    fun heartBeats(heartBeatDTO: HeartBeatDTO) {
        val json = jacksonObjectMapper().writeValueAsString(heartBeatDTO)
        kafkaTemplate.send(TOPIC_HEART_BEATS, json)
    }

    fun confirmEntry(confirmEntryDTO: ConfirmEntryDTO) {
        val json = jacksonObjectMapper().writeValueAsString(confirmEntryDTO)
        kafkaTemplate.send(TOPIC_CONFIRM_ENTRY, json)
    }
}