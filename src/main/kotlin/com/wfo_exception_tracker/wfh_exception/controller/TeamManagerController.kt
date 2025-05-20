package com.wfo_exception_tracker.wfh_exception.controller


import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dto.WfhRequestDto
import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.FullEmployeeDetailsWithApproval
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.dtos.WfhRequestForTm
import com.wfo_exception_tracker.wfh_exception.exception.ResourceNotFoundException
import com.wfo_exception_tracker.wfh_exception.service.TeamManagerService
import com.wfo_exception_tracker.wfh_exception.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/tm")
@CrossOrigin(origins = ["http://localhost:5173"])
class TeamManagerController(
    private val teamManagerService: TeamManagerService,
    private val userService: UserService
) {

    @GetMapping("/requests")
    fun getPendingRequests(): List<WfhRequestForTm> {
       return teamManagerService.getRequestsForTeamManager(AuthUtils.getCurrentUserId())
    }

    @GetMapping("/request-details/{requestId}")
    fun getTmRequestDetails(
        @PathVariable requestId: Long
    ): ResponseEntity<FullEmployeeDetailsWithApproval> {
        return ResponseEntity.ok(
            userService.getFullEmployeeDetailsByRequestId(requestId, ApprovalLevel.TEAM_MANAGER)
                ?: throw ResourceNotFoundException("Request not found")
        )
    }

    @PostMapping("/{requestId}/approve")
    fun approveRequestByTeamManager( @PathVariable requestId: Long ): ResponseEntity<String> {

        return ResponseEntity.ok(userService.approveOrRejectRequest(
            requestId,
            AuthUtils.getCurrentUserId(),
            ApprovalLevel.TEAM_MANAGER,
            WorkflowStatus.APPROVED))
    }

    @PostMapping("/{requestId}/reject")
    fun rejectRequestByTeamManager( @PathVariable requestId: Long ) : ResponseEntity<String> {
        return ResponseEntity.ok(userService.approveOrRejectRequest(requestId,AuthUtils.getCurrentUserId(),
            ApprovalLevel.TEAM_MANAGER,
            WorkflowStatus.REJECTED))
    }

    @GetMapping("/calendar")
    fun getWFHCalendar(): ResponseEntity<Map<String, Int>> {
        return ResponseEntity.ok(teamManagerService.getWFHCalendarData(AuthUtils.getCurrentUserId()))
    }

    @GetMapping("/employees-on-date")
    fun getEmployeesForDate(
        @RequestParam date: LocalDate
    ): ResponseEntity<List<Map<String, Any>>> {
        val employees = teamManagerService.getApprovedEmployeesByDate(AuthUtils.getCurrentUserId(), date)
        return ResponseEntity.ok(employees)
    }


}