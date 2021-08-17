package com.sd.no.controller

import com.sd.no.domain.Log
import com.sd.no.domain.State
import com.sd.no.repository.LogRepository
import com.sd.no.repository.StateRepository
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/no")
class Controller (
    private val electionController: ElectionController,
    private val logRepository: LogRepository,
    private val stateRepository: StateRepository
) {

    @PostMapping("/log")
    fun setLog(log: String) {
        electionController.log = log
        logRepository.save(Log(state = log))
    }

    @GetMapping("/log")
    fun getLog():ResponseEntity<List<Log>> {
        return ResponseEntity(logRepository.findAll(), HttpStatus.OK)
    }

    @GetMapping("/state")
    fun getState():ResponseEntity<List<State>> {
        return ResponseEntity(stateRepository.findAll(), HttpStatus.OK)
    }
}