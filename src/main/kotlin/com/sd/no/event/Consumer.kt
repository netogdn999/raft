package com.sd.no.event

import com.sd.no.controller.Controller
import com.sd.no.controller.Type
import com.sd.no.domain.State
import com.sd.no.repository.StateRepository
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import java.net.Proxy
import kotlin.random.Random

@Component
class Consumer (
        private val stateRepository: StateRepository,
        private val producer: Producer
) {

    companion object {
        lateinit var job1: Thread
        private val maxIntances = 4
        var type: Type = Type.seguidor
        var election: MutableMap<Int, Int> = mutableMapOf(0 to 0)
        var term: Int = 0
        var starTime: Long = 0
        var timeoutElection: Long = 0
        var isTimeouted = false
        var leader: String? = null
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
                if(!election.containsKey(term)) {
                    type = Type.candidato
                    term++
                    election[term] = (election[term] ?: 0) + 1
                    producer.requestVotes()
                    job1.start()
                }
            }
        }
        job1.start()
    }

    @KafkaListener(topics = ["requestVotes"])
    fun receiveVotes(@Payload payload: String) {
        election[term] = (election[term] ?: 0) + 1
        if(election[term] != null && election[term]!! > maxIntances / 2) {
            type = Type.lider
            producer.appendEntries()
        } else {
            timeoutElection = Random(System.currentTimeMillis()).nextLong(from = 150, until = 300)
            starTime = System.currentTimeMillis()
            if(!job1.isAlive)
                job1.start()
        }
    }

    @KafkaListener(topics = ["appendEntries"])
    fun appendEntries(@Payload payload: String) {
        val termReceived: Int = Integer.parseInt(payload)
        val leaderReceived: String = payload
        if(type == Type.lider && termReceived > term) {
            type = Type.seguidor
            term = termReceived
        }
        leader = leaderReceived
        timeoutElection = Random(System.currentTimeMillis()).nextLong(from = 150, until = 300)
        starTime = System.currentTimeMillis()
        if(!job1.isAlive)
            job1.start()
    }

    @KafkaListener(topics = ["heartBeats"])
    fun heartBeats(@Payload payload: String) {
        stateRepository.save(State(state = 0))
    }
}