package com.sd.no.controller

import com.sd.no.dto.HeartBeatDTO
import com.sd.no.event.Producer
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import kotlin.random.Random

enum class Type {
    SEGUIDOR,
    CANDIDATO,
    LIDER
}

@Component
class ElectionController(
        private val producer: Producer
) {
    @Value("\${server.port}")
    var serverPort: String = ""
    val maxInstances = 3
    var type: Type = Type.SEGUIDOR
    var receivedVotes: Int = 0
    var election: MutableMap<Int, Int> = mutableMapOf()
    var term: Int = 0
    var starTime: Long = 0
    var timeoutElection: Long = 0
    var isTimeOut = false
    var leader: String = ""
    var log:String = ""
    var confirmEntry:Int = 0

    init {
        Thread {
            kotlin.run {
                timeoutElection()
                while(true) {
                    if (type != Type.LIDER) {
                        while (!isTimeOut) {
                            isTimeOut = System.currentTimeMillis() - starTime >= timeoutElection
                        }
                        receivedVotes = 0
                        confirmEntry = 0
                        type = Type.CANDIDATO
                        receivedVotes++
                        producer.requestVotes()
                        term++
                        election[term] = (election[term] ?: 0) + 1
                        timeoutElection()
                    }
                }
            }
        }.start()
    }

    final fun timeoutElection() {
        timeoutElection = Random(System.currentTimeMillis()).nextLong(from = 15000, until = 30000)
        starTime = System.currentTimeMillis()
        isTimeOut = false
    }

    @Scheduled(fixedDelay = 5000)
    fun heartBeats() {
        if(type == Type.LIDER) {
            producer.heartBeats(HeartBeatDTO(
                serverPort,
                term,
                log
            ))
            log = ""
        }
    }
}