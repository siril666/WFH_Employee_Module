package com.wfo_exception_tracker.wfh_exception.dto

import java.time.LocalDate

interface WfhRequestReportView {
    fun getRequestId(): Long?
    fun getIbsEmpId(): Long?
    fun getEmployeeName(): String
    fun getTeam(): String
    fun getRequestedStartDate(): LocalDate
    fun getRequestedEndDate(): LocalDate
    fun getCategoryOfReason(): String
    fun getPriority(): String
    fun getStatus(): String
}