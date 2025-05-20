package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import com.wfo_exception_tracker.wfh_exception.dtos.WfhRequestForHr
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeInfoRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class HrDashboardService(
    private val approvalWorkflowRepository: ApprovalWorkflowRepository,
    private val wfhRequestRepository: WfhRequestRepository,
    private val employeeInfoRepository: EmployeeInfoRepository
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



}