package com.wfo_exception_tracker.wfh_exception.dtos

import com.wfo_exception_tracker.wfh_exception.entity.EmployeeMaster
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest

data class FullEmployeeDetailsWithApproval(
    val wfhRequest: WfhRequest,
    val employeeMaster: EmployeeMaster,
    val currentUserApprovalStatus: ApprovalStatusDto
)
