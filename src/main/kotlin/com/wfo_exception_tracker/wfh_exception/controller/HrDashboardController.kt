package com.wfo_exception_tracker.wfh_exception.controller

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dto.GraphReportView
import com.wfo_exception_tracker.wfh_exception.dto.WfhRequestReportDTO
import com.wfo_exception_tracker.wfh_exception.dto.WfhRequestReportView
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
@CrossOrigin(origins = ["http://localhost:5173"])
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



    //*******************hr report and audit**********************************

    @GetMapping("/approved-by-sdm-or-hr")
    fun getRequestsApprovedBySdmOrHr(): ResponseEntity<List<WfhRequestReportDTO>> {
        val results = hrDashboardService.getApprovedRequestsBySdmOrHr()
        return ResponseEntity.ok(results)
    }

    @GetMapping("/requests/search")
    fun searchRequests(
        @RequestParam empId: Long?,
        @RequestParam empName: String?,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?,
        @RequestParam categoryOfReason: String?
    ): ResponseEntity<List<WfhRequestReportDTO>> {
        val results = hrDashboardService.searchApprovedRequests(empId, empName, date,categoryOfReason)
        return ResponseEntity.ok(results)
    }



    @GetMapping("/requests/search/excel")
    fun downloadExcel(
        @RequestParam(required = false) empId: String?,
        @RequestParam(required = false) empName: String?,
        @RequestParam(required = false) date: String?,
        @RequestParam(required=false) categoryOfReason: String?,
        response: HttpServletResponse
    ) {
        val localDate = date?.let { LocalDate.parse(it) }

        val excelBytes = hrDashboardService.generateExcelReport(empId?.toLongOrNull(), empName, localDate,categoryOfReason)

        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.setHeader("Content-Disposition", "attachment; filename=wfh_requests.xlsx")
        response.outputStream.write(excelBytes)
        response.outputStream.flush()
    }




    @GetMapping("/search-by-team-and-date")
    fun getReportByTeamAndDate(
        @RequestParam(required = false) teamName: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate?
    ): ResponseEntity<List<WfhRequestReportView>> {
        val results = hrDashboardService.getReportByTeamAndDate(teamName, date)
        return ResponseEntity.ok(results)
    }


    @GetMapping("/requests/search-by-team/excel")
    fun downloadExcelByTeam(
        @RequestParam(required = false) teamName: String?,
        @RequestParam(required = false) date: String?,
        response: HttpServletResponse
    ) {
        val localDate = date?.let { LocalDate.parse(it) }

        val excelBytes = hrDashboardService.generateExcelByTeamAndDate(teamName, localDate)

        response.contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        response.setHeader("Content-Disposition", "attachment; filename=wfh_team_requests.xlsx")
        response.outputStream.write(excelBytes)
        response.outputStream.flush()
    }




    @GetMapping("/graph-report")
    fun getRequestsReport(
        @RequestParam period: String,                // 'WEEK', 'MONTH', or 'QUARTER'
        @RequestParam(required = false) teamName: String?  // optional team name filter
    ): List<GraphReportView> {
        return hrDashboardService.getRequestsReport(period, teamName)
    }





}