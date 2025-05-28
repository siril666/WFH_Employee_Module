package com.wfo_exception_tracker.wfh_exception.dtos

import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import java.time.LocalDate
import java.time.LocalDateTime

//data class WfhRequestForTm(
//    val employeeName :String?,
//    val request: WfhRequest,
//    val tmStatus: String,
//    val sdmStatus: String?,
//    val tmActionDate: LocalDate?,
//    val sdmActionDate: LocalDate?
//)


data class WfhRequestForTm(
    val employeeName: String?,
    val request: WfhRequest,
    val tmStatus: String?,
    val sdmStatus: String?,
    val hrStatus: String?,
    val tmActionDate: LocalDate?,
    val sdmActionDate: LocalDate?,
    val hrActionDate: LocalDate?
)