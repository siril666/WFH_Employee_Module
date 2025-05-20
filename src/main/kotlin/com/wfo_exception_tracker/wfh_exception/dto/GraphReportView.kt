package com.wfo_exception_tracker.wfh_exception.dto

interface GraphReportView {
    fun getYear(): Int
    fun getPeriodValue(): Int
    fun getTeam(): String
    fun getStatus(): String     // Add this
    fun getRequestCount(): Int
}