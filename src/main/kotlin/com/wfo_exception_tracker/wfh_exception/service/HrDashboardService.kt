package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import com.wfo_exception_tracker.wfh_exception.dtos.WfhRequestForHr
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.dto.toDto
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.dto.GraphReportView
import com.wfo_exception_tracker.wfh_exception.dto.WfhRequestReportDTO
import com.wfo_exception_tracker.wfh_exception.dto.WfhRequestReportView
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeInfoRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeMasterRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.time.LocalDate

@Service
class HrDashboardService(
    private val approvalWorkflowRepository: ApprovalWorkflowRepository,
    private val wfhRequestRepository: WfhRequestRepository,
    private val employeeInfoRepository: EmployeeInfoRepository,
    private val employeeMasterRepository: EmployeeMasterRepository
) {
    private val logger = LoggerFactory.getLogger(HrDashboardService::class.java)


    fun getHrRequests(): List<WfhRequestForHr> {
        // 1. Get all requests approved by both TM and SDM
        val tmApproved = approvalWorkflowRepository
            .findByLevelAndStatus(ApprovalLevel.TEAM_MANAGER, WorkflowStatus.APPROVED)
            .map { it.requestId }

        val sdmApproved = approvalWorkflowRepository
            .findByLevelAndStatus(ApprovalLevel.SDM, WorkflowStatus.APPROVED)
            .map { it.requestId }

        // 2. Get intersection of approved requests
        val fullyApprovedRequestIds = tmApproved.toSet()
            .intersect(sdmApproved.toSet())
            .toList()
        // 3. Get these requests and validate workflow integrity
        return wfhRequestRepository.findByRequestIdIn(fullyApprovedRequestIds)
            .sortedByDescending { it.requestedStartDate }
            .map { request ->
                    // Validate workflow records exist for non-pending statuses
                if (request.status != RequestStatus.PENDING) {
                    val hrApproval = approvalWorkflowRepository
                        .findByRequestIdAndLevel(request.requestId, ApprovalLevel.HR_MANAGER)
                        ?: throw IllegalStateException("HR workflow record missing for request ${request.requestId}")

                    // Additional validation if needed
                    if (hrApproval.status.name != request.status.name) {
                        throw IllegalStateException("Status mismatch between wfh_request and approval_workflow for request ${request.requestId}")
                    }
                }
                WfhRequestForHr(
                    request = request,
                    hrStatus = request.status.name,
                    hrUpdatedDate = approvalWorkflowRepository
                        .findByRequestIdAndLevel(request.requestId, ApprovalLevel.HR_MANAGER)
                        ?.updatedDate,
                    userName = employeeInfoRepository.findByIbsEmpId(request.ibsEmpId)?.userName,
                    teamOwnerName = request.teamOwnerId?.let { employeeInfoRepository.findByIbsEmpId(it)?.userName }
                )
            }
    }



    //******************hr report and audit****************************

    fun getApprovedRequestsBySdmOrHr(): List<WfhRequestReportDTO> {
        val requestIds = approvalWorkflowRepository.findRequestsApprovedBySdmOrHr()
        val requests = wfhRequestRepository.findByRequestIdIn(requestIds)

        return requests.map { request ->
            val employeeName = employeeInfoRepository.findByIbsEmpId(request.ibsEmpId)?.userName ?: "Unknown"
            val Team = employeeMasterRepository.findByIbsEmpId(request.ibsEmpId)?.team ?: "Unknown"

            request.toDto(employeeName, Team)
        }
    }

    fun searchApprovedRequests(
        empId: Long?,
        empName: String?,
        date: LocalDate?,
        categoryOfReason:String?
    ): List<WfhRequestReportDTO> {
        val filteredRequests = wfhRequestRepository.findFilteredApprovedRequests(empId, empName, date,categoryOfReason)

        return filteredRequests.map { request ->
            val employeeName = employeeInfoRepository.findByIbsEmpId(request.ibsEmpId)?.userName ?: "Unknown"
            val teamName = employeeMasterRepository.findByIbsEmpId(request.ibsEmpId)?.team ?: "Unknown"
            request.toDto(employeeName, teamName)
        }
    }





    fun getReportByTeamAndDate(teamName: String?, date: LocalDate?): List<WfhRequestReportView> {
        return wfhRequestRepository.findApprovedRequestsByTeamAndDate(teamName, date)
    }










    fun generateExcelReport(empId: Long?, empName: String?, date: LocalDate?, categoryOfReason: String?): ByteArray {
        val data = searchApprovedRequests(empId, empName, date,categoryOfReason)

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("WFH Requests")

        val header = sheet.createRow(0)
        val columns = listOf("Request ID", "Employee ID", "Employee Name", "Team", "Start Date", "End Date", "Reason Category", "Priority", "Status")
        columns.forEachIndexed { i, title -> header.createCell(i).setCellValue(title) }

        data.forEachIndexed { i, dto ->
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(dto.requestId.toDouble())
            row.createCell(1).setCellValue(dto.ibsEmpId.toDouble())
            row.createCell(2).setCellValue(dto.employeeName)
            row.createCell(3).setCellValue(dto.team)
            row.createCell(4).setCellValue(dto.requestedStartDate.toString())
            row.createCell(5).setCellValue(dto.requestedEndDate.toString())
            row.createCell(6).setCellValue(dto.categoryOfReason)
            row.createCell(7).setCellValue(dto.priority.toString())
            row.createCell(8).setCellValue(dto.status.toString())

        }

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return outputStream.toByteArray()
    }





    fun generateExcelByTeamAndDate(teamName: String?, date: LocalDate?): ByteArray {
        val data = getReportByTeamAndDate(teamName, date)

        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("WFH Team Requests")

        val header = sheet.createRow(0)
        val columns = listOf(
            "Request ID", "Employee ID", "Employee Name", "Team", "Start Date", "End Date",
            "Reason Category", "Priority", "Status"
        )
        columns.forEachIndexed { i, title ->
            header.createCell(i).setCellValue(title)
        }

        data.forEachIndexed { i, dto ->
            val row = sheet.createRow(i + 1)
            row.createCell(0).setCellValue(dto.getRequestId()!!.toDouble())
            row.createCell(1).setCellValue(dto.getIbsEmpId()!!.toDouble())
            row.createCell(2).setCellValue(dto.getEmployeeName())
            row.createCell(3).setCellValue(dto.getTeam())
            row.createCell(4).setCellValue(dto.getRequestedStartDate().toString())
            row.createCell(5).setCellValue(dto.getRequestedEndDate().toString())
            row.createCell(6).setCellValue(dto.getCategoryOfReason())
            row.createCell(7).setCellValue(dto.getPriority())
            row.createCell(8).setCellValue(dto.getStatus())
        }

        val outputStream = ByteArrayOutputStream()
        workbook.write(outputStream)
        workbook.close()

        return outputStream.toByteArray()
    }


    private val validPeriods = setOf("WEEK", "MONTH", "QUARTER")

    fun getRequestsReport(period: String, teamName: String?): List<GraphReportView> {
        val normalizedPeriod = period.uppercase()
        if (normalizedPeriod !in validPeriods) {
            throw IllegalArgumentException("Invalid period value: $period. Allowed values are WEEK, MONTH, QUARTER.")
        }

        return wfhRequestRepository.getRequestsReport(normalizedPeriod, teamName)
    }








}