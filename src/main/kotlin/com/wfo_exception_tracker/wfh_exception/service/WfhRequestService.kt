package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.exception.ResourceNotFoundException
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.springframework.data.crossstore.ChangeSetPersister
import org.springframework.stereotype.Service

@Service
class WfhRequestService(
    private val wfhRequestRepository: WfhRequestRepository,
     private val approvalWorkflowRepository: ApprovalWorkflowRepository
) {
    fun createWfhRequest(request: WfhRequest): WfhRequest {
        return wfhRequestRepository.save(request)
    }

    fun updateWfhRequest(request: WfhRequest): WfhRequest {
        return wfhRequestRepository.save(request)
    }

    fun getRequestById(id: Long): WfhRequest? {
        return wfhRequestRepository.findById(id).orElse(null)
    }

    fun getRequestsByEmployee(ibsEmpId: Long): List<WfhRequest> {
        return wfhRequestRepository.findByIbsEmpId(ibsEmpId)
    }

//    fun cancelWfhRequest(requestId: Long) {
//        wfhRequestRepository.deleteById(requestId)
//        approvalWorkflowRepository.deleteByRequestId(requestId)
//    }

    fun cancelWfhRequest(requestId: Long) {
        val request = wfhRequestRepository.findById(requestId)
            .orElseThrow { ResourceNotFoundException("WFH request not found with id: $requestId") }

        // Check if request has any approved approvals
        val approvals = approvalWorkflowRepository.findByRequestId(requestId)
        if (approvals.any { it.status == WorkflowStatus.APPROVED }) {
            throw IllegalStateException("Cannot cancel request that has been approved")
        }

        // Proceed with cancellation
        wfhRequestRepository.delete(request)

        // Also delete any existing approval workflows for this request
        approvalWorkflowRepository.deleteAll(approvals)
    }


}