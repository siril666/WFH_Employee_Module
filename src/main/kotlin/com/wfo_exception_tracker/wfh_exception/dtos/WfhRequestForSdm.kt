package com.wfo_exception_tracker.wfh_exception.dtos

import com.wfo_exception_tracker.wfh_exception.entity.ApprovalWorkflow
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import java.time.LocalDate

data class WfhRequestForSdm(
    val employeeName:String?,
    val request: WfhRequest,
    val sdmStatus: String,
    val hrStatus: String?,
    val sdmUpdatedDate: LocalDate?,
    val teamOwnerName:String?
)