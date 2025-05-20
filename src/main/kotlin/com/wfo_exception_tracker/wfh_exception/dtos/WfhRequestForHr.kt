package com.wfo_exception_tracker.wfh_exception.dtos

import com.wfo_exception_tracker.wfh_exception.entity.ApprovalWorkflow
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import java.time.LocalDate

data class WfhRequestForHr(
    val request: WfhRequest,
    val hrStatus: String,
    val hrUpdatedDate: LocalDate?,
    val userName : String?,
    val teamOwnerName : String?
)