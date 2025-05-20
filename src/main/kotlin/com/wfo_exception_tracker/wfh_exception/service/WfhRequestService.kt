package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
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

    fun cancelWfhRequest(requestId: Long) {
        wfhRequestRepository.deleteById(requestId)
        approvalWorkflowRepository.deleteByRequestId(requestId)
    }

}