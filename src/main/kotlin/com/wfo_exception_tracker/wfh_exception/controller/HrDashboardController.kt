package com.wfo_exception_tracker.wfh_exception.controller

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils


import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.FullEmployeeDetailsWithApproval
import com.wfo_exception_tracker.wfh_exception.dtos.WfhRequestForHr
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.exception.ResourceNotFoundException
import com.wfo_exception_tracker.wfh_exception.service.HrDashboardService
import com.wfo_exception_tracker.wfh_exception.service.UserService
import jakarta.servlet.http.HttpServletResponse
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/hr")
@CrossOrigin(origins = ["*"])
class HrDashboardController(
    private val hrDashboardService: HrDashboardService,
    private val userService: UserService,
) {

    @GetMapping("/requests")
    fun getAllRequestsForHR(): List<WfhRequestForHr> {
        return hrDashboardService.getHrRequests()
    }

    @GetMapping("/request-details/{requestId}")
    fun getSdmRequestDetails( @PathVariable requestId: Long ): ResponseEntity<FullEmployeeDetailsWithApproval> {
        return ResponseEntity.ok(
            userService.getFullEmployeeDetailsByRequestId(requestId, ApprovalLevel.HR_MANAGER)
                ?: throw ResourceNotFoundException("Request not found")
        )
    }

    @PostMapping("/{requestId}/approve")
    fun approveRequestByHR( @PathVariable requestId: Long ): ResponseEntity<String> {
        return ResponseEntity.ok(userService.approveOrRejectRequest(
            requestId,
            AuthUtils.getCurrentUserId(),
            ApprovalLevel.HR_MANAGER,
            WorkflowStatus.APPROVED))
    }

    @PostMapping("/{requestId}/reject")
    fun rejectRequestByHR( @PathVariable requestId: Long ) : ResponseEntity<String> {
        return ResponseEntity.ok(userService.approveOrRejectRequest(requestId,AuthUtils.getCurrentUserId(),
            ApprovalLevel.HR_MANAGER,
            WorkflowStatus.REJECTED))
    }






}