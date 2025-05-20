package com.wfo_exception_tracker.wfh_exception.controller

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dtos.*
import com.wfo_exception_tracker.wfh_exception.exception.ResourceNotFoundException
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeInfoRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import com.wfo_exception_tracker.wfh_exception.service.SdmDashboardService
import com.wfo_exception_tracker.wfh_exception.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@CrossOrigin(origins = ["*"])
@RequestMapping("/sdm")
class SdmDashboardController(
    private val sdmDashboardService: SdmDashboardService,
    private val wfhRequestRepository : WfhRequestRepository,
    private val employeeInfo: EmployeeInfoRepository,
    private val userService: UserService
) {

    @GetMapping("/requests")
    fun getRequestsApprovedByTM(): List<WfhRequestForSdm> {
        return sdmDashboardService.getRequestsForSdm(AuthUtils.getCurrentUserId())
    }

    @GetMapping("/request-details/{requestId}")
    fun getSdmRequestDetails( @PathVariable requestId: Long ): ResponseEntity<FullEmployeeDetailsWithApproval> {
        return ResponseEntity.ok(
            userService.getFullEmployeeDetailsByRequestId(requestId, ApprovalLevel.SDM)
                ?: throw ResourceNotFoundException("Request not found")
        )
    }

    @PostMapping("/{requestId}/approve")
    fun approveRequestForSdm(@PathVariable requestId: Long ): ResponseEntity<String> {

        return ResponseEntity.ok(userService.approveOrRejectRequest(
            requestId,
            AuthUtils.getCurrentUserId(),
            ApprovalLevel.SDM,
            WorkflowStatus.APPROVED))
    }

    @PostMapping("/{requestId}/reject")
    fun rejectRequestBySdm( @PathVariable requestId: Long ) : ResponseEntity<String> {
        return ResponseEntity.ok(userService.approveOrRejectRequest(
            requestId,
            AuthUtils.getCurrentUserId(),
            ApprovalLevel.SDM,
            WorkflowStatus.REJECTED))
    }

    @GetMapping("/calendar")
    fun getSdmCalendar(): List<SdmCalendarDay> {
        return sdmDashboardService.generateSdmCalendar(AuthUtils.getCurrentUserId())
    }

    @GetMapping("/calendar/details")
    fun getDateDetails( @RequestParam date: String ): List<SdmRequestDetail> {
        return sdmDashboardService.getSdmDateDetails(
            LocalDate.parse(date),
            AuthUtils.getCurrentUserId()
        )
    }
}