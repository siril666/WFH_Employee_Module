package com.wfo_exception_tracker.wfh_exception.dtos

import java.time.LocalDate

data class SdmCalendarDay(
    val date: LocalDate,
    val teamStatus: Map<Long, SdmDayStatus> // teamOwnerId to status counts
)

data class SdmDayStatus(
    val approvedBySdm: Int,
    val pendingWithSdm: Int,
    val rejectedBySdm: Int,
    val pendingWithHr: Int // After SDM approval
)

data class SdmRequestDetail(
    val requestId: Long,
    val employeeId: Long,
    val employeeName: String,
    val sdmStatus: String, // "APPROVED", "REJECTED", or "PENDING"
    val hrStatus: String?, // Only if SDM approved
    val startDate: LocalDate,
    val endDate: LocalDate,
    val reason: String?,
    val teamOwnerId:Long?
)
