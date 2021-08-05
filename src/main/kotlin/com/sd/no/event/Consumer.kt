package com.sd.no.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sd.no.controller.ElectionController
import com.sd.no.controller.Type
import com.sd.no.domain.State
import com.sd.no.dto.LogDTO
import com.sd.no.repository.StateRepository
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class Consumer (
        private val stateRepository: StateRepository,
        private val electionController: ElectionController,
        private val producer: Producer
) {

    @KafkaListener(topics = ["requestVotes"], groupId = "1")
    fun receiveVotes(@Payload payload: String) {
        val logDTO: LogDTO = jacksonObjectMapper().readValue(payload)
        if(logDTO.origin != "12333") {
            val term = electionController.term
            val election = electionController.election
            val maxIntances = electionController.maxIntances
            election[term] = (election[term] ?: 0) + 1
            if (election[term] != null && election[term]!! > maxIntances / 2) {
                electionController.type = Type.lider
                producer.appendEntries()
            } else {
                electionController.timeoutElection()
            }
        }
    }

    @KafkaListener(topics = ["appendEntries"])
    fun appendEntries(@Payload payload: String) {
        val logDTO: LogDTO = jacksonObjectMapper().readValue(payload)
        if(logDTO.origin != "12333") {
            val termReceived: Int = Integer.parseInt(payload)
            val leaderReceived: String = payload
            val term = electionController.term
            if (electionController.type == Type.lider && termReceived > term) {
                electionController.type = Type.seguidor
                electionController.term = termReceived
            }
            electionController.leader = leaderReceived
            electionController.timeoutElection()
        }
    }

    @KafkaListener(topics = ["heartBeats"])
    fun heartBeats(@Payload payload: String) {
        stateRepository.save(State(state = 0))
    }
}