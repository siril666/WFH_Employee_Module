package com.wfo_exception_tracker.wfh_exception.controller


import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dto.WfhRequestDto
import com.wfo_exception_tracker.wfh_exception.dtos.*
import com.wfo_exception_tracker.wfh_exception.exception.ResourceNotFoundException
import com.wfo_exception_tracker.wfh_exception.service.TeamManagerService
import com.wfo_exception_tracker.wfh_exception.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/tm")
@CrossOrigin(origins = ["*"])
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
    fun getTeamManagerCalendar(): ResponseEntity<List<TeamManagerCalendarDay>> {
        val currentUserId = AuthUtils.getCurrentUserId()
        val calendarData = teamManagerService.getTeamManagerCalendar(currentUserId)
        return ResponseEntity.ok(calendarData)
    }
    @GetMapping("/employees-on-date")
    fun getEmployeesForDate(
        @RequestParam date: LocalDate
    ): ResponseEntity<List<Map<String, Any>>> {
        val employees = teamManagerService.getApprovedEmployeesByDate(AuthUtils.getCurrentUserId(), date)
        return ResponseEntity.ok(employees)
    }


}