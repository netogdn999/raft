package com.sd.no.repository

import com.sd.no.domain.State
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface StateRepository : JpaRepository<State, Int>