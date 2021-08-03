package com.sd.no.event

import com.sd.no.controller.Controller
import com.sd.no.controller.Type
import com.sd.no.domain.State
import com.sd.no.repository.StateRepository
import org.slf4j.LoggerFactory
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class Consumer (
        private val stateRepository: StateRepository,
        private val producer: Producer
) {

    companion object {
        lateinit var job1: Thread
        var type: Type = Type.seguidor
        var term: Int = 0
        var votes: Int = 0
        var isVoted = false
        var starTime: Long = 0
        var timeoutElection: Long = 0
        var isTimeouted = false
        private val logger = LoggerFactory.getLogger(Controller::class.java)
    }

    init {
        timeoutElection = Random(System.currentTimeMillis()).nextLong(from = 150, until = 300)
        starTime = System.currentTimeMillis()
        job1 = Thread {
            kotlin.run {
                while(!isTimeouted) {
                    isTimeouted = System.currentTimeMillis() - starTime >= timeoutElection
                }
                if(!isVoted) {
                    isVoted = true
                    votes++
                    type = Type.candidato
                    term++
                    producer.requestVotes()
                }
            }
        }
        job1.start()
    }

    //@KafkaListener(topics = ["requestVotes"])
    fun requestVotes(@Payload payload: String) {
        if(isVoted) {
            // todo criar metodo para retornar
        } else {
            // todo criar metodo para votar
            isVoted = true
            term++
            timeoutElection = Random(System.currentTimeMillis()).nextLong(from = 150, until = 300)
            starTime = System.currentTimeMillis()
            if(!job1.isAlive)
                job1.start()
        }
    }

    //@KafkaListener(topics = ["appendEntries"])
    fun appendEntries(@Payload payload: String) {

    }

    //@KafkaListener(topics = ["heartBeats"])
    fun heartBeats(@Payload payload: String) {
        stateRepository.save(State(state = 0))
    }
}