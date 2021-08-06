package com.sd.no.controller

import com.sd.no.event.Consumer
import com.sd.no.event.Producer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class ElectionController(
        private val producer: Producer
) {
    @Value("\${server.port}")
    var serverPort: String = ""
    val maxInstances = 3
    var type: Type = Type.SEGUIDOR
    var election: MutableMap<Int, Int> = mutableMapOf()
    var term: Int = 0
    var starTime: Long = 0
    var timeoutElection: Long = 0
    var isTimeOut = false
    var leader: String = ""
    private val logger = LoggerFactory.getLogger(ElectionController::class.java)

    init {
        timeoutElection()
        Thread {
            kotlin.run {
                while(true) {
                    if (type != Type.LIDER) {
                        while (!isTimeOut) {
                            isTimeOut = System.currentTimeMillis() - starTime >= timeoutElection
                        }
                        if (!election.containsKey(term)) {
                            type = Type.CANDIDATO
                            election[term] = (election[term] ?: 0) + 1
                            term++
                            isTimeOut = false
                            timeoutElection()
                            producer.requestVotes()
                        }
                    }
                }
            }
        }.start()
    }

    final fun timeoutElection() {
        timeoutElection = Random(System.currentTimeMillis()).nextLong(from = 150, until = 300)
        starTime = System.currentTimeMillis()
    }

    @Scheduled(fixedDelay = 50)
    fun heartBeats() {
        if(type == Type.LIDER) {
            logger.info("HeartBeat {}", leader)
            producer.heartBeats()
        }
    }
}