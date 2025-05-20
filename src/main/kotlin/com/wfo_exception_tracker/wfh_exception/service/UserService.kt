package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.dtos.*
import com.wfo_exception_tracker.wfh_exception.entity.ApprovalWorkflow
import com.wfo_exception_tracker.wfh_exception.entity.EmployeeInfo
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeInfoRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeMasterRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.springframework.stereotype.Service
import java.time.LocalDate


@Service
class UserService (
    private val employeeInfoRepository: EmployeeInfoRepository,
    private val employeeMasterRepository: EmployeeMasterRepository,
    private val wfhRequestRepository: WfhRequestRepository,
    private val approvalWorkflowRepository: ApprovalWorkflowRepository
){

    fun getProfile(empId: Long): EmployeeInfo? =
        employeeInfoRepository.findByIbsEmpId(empId)

    fun getFullEmployeeDetailsByRequestId(requestId: Long, approval: ApprovalLevel): FullEmployeeDetailsWithApproval? {
        // 1. Get base data
        val wfhRequest = wfhRequestRepository.findByRequestId(requestId) ?: return null
        val employee = employeeMasterRepository.findByIbsEmpId(wfhRequest.ibsEmpId) ?: return null
        val approvals = approvalWorkflowRepository.findByRequestId(requestId)

        // 2. Validate approval hierarchy
        when (approval) {
            ApprovalLevel.SDM -> {
                val tmApproved = approvals.any { it.level == ApprovalLevel.TEAM_MANAGER && it.status == WorkflowStatus.APPROVED }
                if (!tmApproved) throw IllegalStateException("TM approval required before SDM review")
            }
            ApprovalLevel.HR_MANAGER -> {
                val tmApproved = approvals.any { it.level == ApprovalLevel.TEAM_MANAGER && it.status == WorkflowStatus.APPROVED }
                val sdmApproved = approvals.any { it.level == ApprovalLevel.SDM && it.status == WorkflowStatus.APPROVED }
                if (!(tmApproved && sdmApproved)) throw IllegalStateException("Both TM and SDM approvals required before HR review")
            }
            else -> {} // No validation needed for TM
        }

        // 3. Build approval status with names
        fun ApprovalWorkflow.toStatusDto() = ApprovalStatusDto(
            status = status.toString(),
            updatedDate = updatedDate,
            approverId = approverId
        )

        return FullEmployeeDetailsWithApproval(
            wfhRequest = wfhRequest,
            employeeMaster = employee,
            currentUserApprovalStatus = approvals
                .firstOrNull { it.level == approval }
                ?.toStatusDto()
                ?: ApprovalStatusDto("PENDING", null, null)
        )
    }

    fun approveOrRejectRequest(requestId: Long, approverId: Long, approvalLevel : ApprovalLevel, workflowStatus: WorkflowStatus): String {

        val approverInfo = employeeInfoRepository.findByIbsEmpId(approverId)
        val currentApprovalName = approverInfo?.userName ?: "Unknown Approver"

        if(workflowStatus == WorkflowStatus.REJECTED){
            val request = wfhRequestRepository.findById(requestId).orElseThrow()
            request.status = RequestStatus.REJECTED
            wfhRequestRepository.save(request)
        }

        if((workflowStatus == WorkflowStatus.APPROVED) && (approvalLevel == ApprovalLevel.HR_MANAGER)){
            val request = wfhRequestRepository.findById(requestId).orElseThrow()
            request.status = RequestStatus.APPROVED
            wfhRequestRepository.save(request)
        }


        approvalWorkflowRepository.save(
            ApprovalWorkflow(
                requestId = requestId,
                approverId = approverId,
                level = approvalLevel,
                status = workflowStatus,
                updatedBy = currentApprovalName,
                updatedDate = LocalDate.now()
            )
        )

        return "Request ${workflowStatus.name} by ${approvalLevel.name} and added to workflow"
    }





}