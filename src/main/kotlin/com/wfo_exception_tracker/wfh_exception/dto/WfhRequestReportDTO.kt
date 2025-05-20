package com.wfo_exception_tracker.wfh_exception.dto
import com.wfo_exception_tracker.wfh_exception.dtos.PriorityLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import java.time.LocalDate

data class WfhRequestReportDTO(
    val requestId: Long,
    val ibsEmpId: Long,
    val employeeName: String,
    val team: String,
    val requestedStartDate: LocalDate,
    val requestedEndDate: LocalDate,
    val categoryOfReason: String,
    val priority: PriorityLevel,
    val status: RequestStatus
)