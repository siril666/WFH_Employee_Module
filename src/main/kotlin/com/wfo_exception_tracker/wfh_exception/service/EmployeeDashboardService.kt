package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.WfhCalendarEntryForEmployee
import com.wfo_exception_tracker.wfh_exception.dtos.WfhRequestWithStatus
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek

@Service
class EmployeeDashboardService(
    private val wfhRequestRepository: WfhRequestRepository,
    private val approvalWorkflowRepository: ApprovalWorkflowRepository
) {

    fun getRequestHistory(empId: Long): List<WfhRequest> =
        wfhRequestRepository.findByIbsEmpId(empId)

    fun getEmployeeRequestsWithStatus(ibsEmpId: Long): List<WfhRequestWithStatus> {
        val requests = wfhRequestRepository.findByIbsEmpId(ibsEmpId)

        return requests.map { request ->
            val approvals = approvalWorkflowRepository.findByRequestId(request.requestId)

            val tmApproval = approvals.firstOrNull { it.level == ApprovalLevel.TEAM_MANAGER }
            val sdmApproval = approvals.firstOrNull { it.level == ApprovalLevel.SDM }
            val hrApproval = approvals.firstOrNull { it.level == ApprovalLevel.HR_MANAGER }

            // Determine current stage and rejection info
            val (currentStage, rejectedBy) = when {
                tmApproval?.status == WorkflowStatus.REJECTED -> "TM" to "Team Manager"
                sdmApproval?.status == WorkflowStatus.REJECTED -> "SDM" to "Senior Delivery Manager"
                hrApproval?.status == WorkflowStatus.REJECTED -> "HR" to "HR Manager"
                hrApproval != null -> "COMPLETED" to null
                sdmApproval != null -> "HR" to null
                tmApproval != null -> "SDM" to null
                else -> "TM" to null
            }

            WfhRequestWithStatus(
                request = request,
                tmStatus = tmApproval?.status?.name ?: "PENDING",
                sdmStatus = sdmApproval?.status?.name ?: "PENDING",
                hrStatus = hrApproval?.status?.name ?: "PENDING",
                currentStage = currentStage,
                rejectedBy = rejectedBy
            )
        }.sortedByDescending { it.request.requestedStartDate }
    }


    fun getCalendarData(currentUserId: Long): List<WfhCalendarEntryForEmployee> {
        val requests = wfhRequestRepository.findByIbsEmpId(currentUserId)

        if (requests.isEmpty()) {
            return emptyList()
        }

        return requests.flatMap { request ->
            val dates = request.requestedStartDate
                .datesUntil(request.requestedEndDate.plusDays(1))
                .filter { date ->
                    val dayOfWeek = date.dayOfWeek
                    dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                }
                .toList()

            dates.map { date ->
                WfhCalendarEntryForEmployee(
                    date = date,
                    status = request.status.name,
                    reason = request.employeeReason,
                    priorityLevel = request.priority,
                    categoryOfReason = request.categoryOfReason
                )
            }
        }.sortedBy { it.date }
    }



}