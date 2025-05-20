package com.wfo_exception_tracker.wfh_exception.dto


import java.time.LocalDate

data class WfhRequestDto(
    val requestId: Long,
    val ibsEmpId: Long,
    val employeeName: String,
    val requestedStartDate: LocalDate,
    val requestedEndDate: LocalDate,
    val employeeReason: String,
    val categoryOfReason: String,
    val status: String,
    val teamOwnerId: Long?,
    val dmId: Long?,
    val termDuration: String?,
    val priority: String,
    val currentLocation: String?,
    val attachmentPath: String?
)