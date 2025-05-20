package com.wfo_exception_tracker.wfh_exception.dto

import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest

fun WfhRequest.toDto(employeeName: String?,
                     team: String?): WfhRequestReportDTO {

    return WfhRequestReportDTO(
        requestId = this.requestId ?: 0L,
        ibsEmpId = this.ibsEmpId,
        employeeName = employeeName ?: "Unknown",
        team = team ?: "Unknown",
        requestedStartDate = this.requestedStartDate,
        requestedEndDate = this.requestedEndDate,
        categoryOfReason = this.categoryOfReason,
        priority = this.priority,
        status = this.status
    )}