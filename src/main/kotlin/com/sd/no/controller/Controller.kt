package com.sd.no.controller

import com.sd.no.domain.Log
import com.sd.no.dto.LogDTO
import com.sd.no.repository.LogRepository
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

enum class Type {
    SEGUIDOR,
    CANDIDATO,
    LIDER
}

@RestController
@RequestMapping("/no")
class Controller (
    private val logRepository: LogRepository
) {

    @PostMapping("/log")
    fun setLog(logDTO: LogDTO) {
        logRepository.save(Log(
            state = logDTO.state
        ))
    }

}