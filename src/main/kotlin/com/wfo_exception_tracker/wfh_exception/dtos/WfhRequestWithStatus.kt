package com.wfo_exception_tracker.wfh_exception.dtos

import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest

data class WfhRequestWithStatus(
    val request: WfhRequest,
    val tmStatus: String,       // "APPROVED", "REJECTED", or "PENDING"
    val sdmStatus: String,      // "APPROVED", "REJECTED", or "PENDING"
    val hrStatus: String,       // "APPROVED", "REJECTED", or "PENDING"
    val currentStage: String,   // "TM", "SDM", "HR", or "COMPLETED"
    val rejectedBy: String?     // Who rejected it (if any)
)