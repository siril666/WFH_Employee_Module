package com.wfo_exception_tracker.wfh_exception.controller

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dtos.WfhCalendarEntryForEmployee
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import com.wfo_exception_tracker.wfh_exception.service.EmployeeDashboardService
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/employee")
@CrossOrigin(origins = ["http://localhost:5173","http://localhost:5174"])
class EmployeeDashboardController(
    private val dashboardService: EmployeeDashboardService,
    private val wfhRequestRepository: WfhRequestRepository
) {

    //1. API to get all requests of the employee
    @GetMapping("/requests")
    fun getRequestHistory(): ResponseEntity<List<WfhRequest>> {
        return ResponseEntity.ok(dashboardService.getRequestHistory(AuthUtils.getCurrentUserId()))
    }

    //2. API to get all request dates of the employee by id for calendar
    @GetMapping("/calendar")
    @Transactional(readOnly = true)
    fun getRequestsForCalendar(): List<WfhCalendarEntryForEmployee> {
        return dashboardService.getCalendarData(AuthUtils.getCurrentUserId())
    }

}