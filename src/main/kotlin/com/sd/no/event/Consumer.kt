package com.sd.no.event

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.sd.no.controller.ElectionController
import com.sd.no.controller.Type
import com.sd.no.domain.Log
import com.sd.no.domain.State
import com.sd.no.dto.AppendEntriesDTO
import com.sd.no.dto.ConfirmEntryDTO
import com.sd.no.dto.HeartBeatDTO
import com.sd.no.dto.SendVotesDTO
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
    fun requestVotes(@Payload origin: String) {
        if(origin != electionController.serverPort) {
            electionController.timeoutElection()
            electionController.term++
            val term = electionController.term
            val election = electionController.election
            if (election[term] == null || election[term]!! <= 0) {
                logger.info("votando em {} ", origin)
                election[term] = (election[term] ?: 0) + 1
                producer.sendVotes(origin)
            }
        }
    }

    @KafkaListener(topics = ["sendVote"])
    fun receiveVotes(@Payload payload: String) {
        val sendVotesDTO: SendVotesDTO = jacksonObjectMapper().readValue(payload)
        if(sendVotesDTO.origin != electionController.serverPort && sendVotesDTO.destination == electionController.serverPort) {
            val maxInstances = electionController.maxInstances
            electionController.receivedVotes++
            if (electionController.receivedVotes > maxInstances / 2) {
                electionController.type = Type.LIDER
                electionController.receivedVotes = 0
                electionController.confirmEntry = 0
                electionController.leader = electionController.serverPort
                logger.info("Novo lider {}", electionController.leader)
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
            val termReceived = appendEntriesDTO.termReceived
            val leaderReceived = appendEntriesDTO.leaderReceived
            val term = electionController.term
            if (electionController.type != Type.LIDER || (electionController.type == Type.LIDER && termReceived > term)) {
                electionController.type = Type.SEGUIDOR
                electionController.term = termReceived
                electionController.leader = leaderReceived
                logger.info("Novo lider {}", electionController.leader)
            }
            electionController.timeoutElection()
        }
    }

    @KafkaListener(topics = ["heartBeats"])
    fun heartBeats(@Payload payload: String) {
        electionController.timeoutElection()
        val heartBeatDTO: HeartBeatDTO = jacksonObjectMapper().readValue(payload)
        if(heartBeatDTO.origin != electionController.serverPort) {
            val term = electionController.term
            if (electionController.type == Type.LIDER && heartBeatDTO.term > term) {
                electionController.type = Type.SEGUIDOR
                electionController.term = heartBeatDTO.term
                electionController.leader = heartBeatDTO.origin
                logger.info("Lider com eleição maior, novo lider {}", electionController.leader)
            }
            if(heartBeatDTO.data.isNotBlank()) {
                val findByState = logRepository.findByState(heartBeatDTO.data)
                if (findByState.isPresent) {
                    stateRepository.save(State(state = heartBeatDTO.data))
                } else {
                    logRepository.save(Log(state = heartBeatDTO.data))
                    producer.confirmEntry(ConfirmEntryDTO(
                            electionController.leader,
                            heartBeatDTO.data
                    ))
                }
            }
        }
    }

    @KafkaListener(topics = ["confirmEntry"])
    fun confirmEntry(@Payload payload: String) {
        val confirmEntryDTO: ConfirmEntryDTO = jacksonObjectMapper().readValue(payload)
        if(confirmEntryDTO.origin == electionController.serverPort) {
            electionController.confirmEntry++
            val maxInstances = electionController.maxInstances - 1
            if (electionController.confirmEntry > maxInstances / 2) {
                stateRepository.save(State(state = confirmEntryDTO.log))
                electionController.log = confirmEntryDTO.log
            }
        }
    }
}