package com.wfo_exception_tracker.wfh_exception.dtos

import java.time.LocalDate

data class TeamManagerDayStatus(
    val pendingWithTeamLead: Int,      // Waiting for TL approval
    val approvedByTeamLead: Int,       // Approved by TL but pending SDM
    val rejectedByTeamLead: Int,       // Rejected by TL
    val pendingWithSdm: Int            // Approved by TL, waiting for SDM
)

data class TeamManagerCalendarDay(
    val date: LocalDate,
    val status: TeamManagerDayStatus
)
