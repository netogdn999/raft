package com.sd.no.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sd.no.controller.ElectionController
import com.sd.no.controller.Type
import com.sd.no.domain.Log
import com.sd.no.domain.State
import com.sd.no.dto.AppendEntriesDTO
import com.sd.no.dto.LogDTO
import com.sd.no.repository.LogRepository
import com.sd.no.repository.StateRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component

@Component
class Consumer (
        private val stateRepository: StateRepository,
        private val logRepository: LogRepository,
        private val electionController: ElectionController,
        private val producer: Producer
) {

    private val logger = LoggerFactory.getLogger(Consumer::class.java)

    @KafkaListener(topics = ["requestVotes"])
    fun requestVotes(@Payload payload: String) {
        val logDTO: LogDTO = jacksonObjectMapper().readValue(payload)
        if(logDTO.origin != electionController.serverPort) {
            electionController.timeoutElection()
            val term = electionController.term
            val election = electionController.election
            if (election[term] == null || election[term]!! <= 0) {
                logger.info("votando em ", electionController.serverPort)
                election[term] = (election[term] ?: 0) + 1
                electionController.term++
                producer.sendVotes()
            }
        }
    }

    @KafkaListener(topics = ["sendVote"])
    fun receiveVotes(@Payload payload: String) {
        val logDTO: LogDTO = jacksonObjectMapper().readValue(payload)
        if(logDTO.origin != electionController.serverPort) {
            val maxInstances = electionController.maxInstances
            electionController.receivedVotes++
            if (electionController.receivedVotes > maxInstances / 2) {
                electionController.receivedVotes = 0
                electionController.leader = electionController.serverPort
                electionController.type = Type.LIDER
                producer.appendEntries(electionController.term, electionController.leader)
            } else {
                electionController.timeoutElection()
            }
        }
    }

    @KafkaListener(topics = ["appendEntries"])
    fun appendEntries(@Payload payload: String) {
        val appendEntriesDTO: AppendEntriesDTO = jacksonObjectMapper().readValue(payload)
        if(appendEntriesDTO.leaderReceived != electionController.serverPort) {
            electionController.timeoutElection()
            val termReceived: Int = appendEntriesDTO.termReceived
            val leaderReceived: String = appendEntriesDTO.leaderReceived
            val term = electionController.term
            if (electionController.type != Type.LIDER || (electionController.type == Type.LIDER && termReceived > term)) {
                electionController.type = Type.SEGUIDOR
                electionController.term = termReceived
                electionController.leader = leaderReceived
                logger.info("Novo lider {}", electionController.leader)
            }
        }
    }

    @KafkaListener(topics = ["heartBeats"])
    fun heartBeats(@Payload payload: String) {
        electionController.timeoutElection()
        if(logRepository.getByState(0).isPresent)
            stateRepository.save(State(state = 0))
        else {
            logRepository.save(Log(state = 0))
            producer.confirmEntry()
        }
    }

    @KafkaListener(topics = ["confirm_entry"])
    fun confirmEntry(@Payload payload: String) {
        stateRepository.save(State(state = 0))
    }
}