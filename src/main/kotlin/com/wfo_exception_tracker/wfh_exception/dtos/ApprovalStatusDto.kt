package com.wfo_exception_tracker.wfh_exception.dtos

import java.time.LocalDate


data class ApprovalStatusDto(
    val status: String, // "PENDING", "APPROVED", or "REJECTED"
    val updatedDate: LocalDate?,
    val approverId: Long?
)