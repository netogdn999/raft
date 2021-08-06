package com.sd.no.repository

import com.sd.no.domain.Log
import com.sd.no.domain.State
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LogRepository : JpaRepository<Log, Int> {
    fun getByState(state: Int): Optional<Log>
}