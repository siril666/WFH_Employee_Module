package com.wfo_exception_tracker.wfh_exception.dtos

import java.time.LocalDate

data class WfhCalendarEntryForEmployee(
    val date: LocalDate,
    val status: String,
    val reason: String
)