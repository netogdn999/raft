package com.sd.no.controller

import com.sd.no.event.Consumer
import com.sd.no.event.Producer
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class ElectionController(
        private val producer: Producer
) {

    val maxIntances = 4
    var type: Type = Type.seguidor
    var election: MutableMap<Int, Int> = mutableMapOf()
    var term: Int = 0
    var starTime: Long = 0
    var timeoutElection: Long = 0
    var isTimeouted = false
    var leader: String? = null
    private val logger = LoggerFactory.getLogger(Consumer::class.java)

    init {
        timeoutElection()
        Thread {
            kotlin.run {
                while(true) {
                    if (type != Type.lider) {
                        while (!isTimeouted) {
                            isTimeouted = System.currentTimeMillis() - starTime >= timeoutElection
                            logger.info("Esperando {}", timeoutElection)
                        }
                        if (!election.containsKey(term)) {
                            logger.info("Elegendo")
                            type = Type.candidato
                            election[term] = (election[term] ?: 0) + 1
                            term++
                            isTimeouted = false
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
}