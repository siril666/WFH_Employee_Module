package com.wfo_exception_tracker.wfh_exception.repository

import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
import com.wfo_exception_tracker.wfh_exception.entity.ApprovalWorkflow
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface ApprovalWorkflowRepository : JpaRepository<ApprovalWorkflow, Long> {

    fun findByRequestId(requestId: Long): List<ApprovalWorkflow>
    fun findByRequestIdAndLevel(requestId: Long, level: ApprovalLevel): ApprovalWorkflow?
    @Query("SELECT a.requestId FROM ApprovalWorkflow a WHERE a.level = 'TEAM_MANAGER' AND a.status = 'APPROVED' AND a.approverId IN :teamOwnerIds")
    fun findApprovedRequestIdsByTeamManager(teamOwnerIds: List<Long>): List<Long>
    fun findByRequestIdIn(requestIds: List<Long>): List<ApprovalWorkflow>
    fun findByRequestIdAndLevelAndStatus(requestId: Long, teamManager: ApprovalLevel, approved: WorkflowStatus): Nothing?





    // Get workflows by approver (e.g., TM, SDM, HR)
    fun findByApproverId(approverId: Long): List<ApprovalWorkflow>

    // Find all approvals at a specific level and status
    fun findByLevelAndStatus(level: ApprovalLevel, status: WorkflowStatus): List<ApprovalWorkflow>

    // Find approved request IDs by TM for SDM to fetch from WfhRequest
    @Query("SELECT a.requestId FROM ApprovalWorkflow a WHERE a.level = 'TEAM_MANAGER' AND a.status = 'APPROVED'")
    fun findApprovedRequestIdsByTeamManager(): List<Long>

    // Optional: Check if a given request is already approved at SDM level
    fun existsByRequestIdAndLevelAndStatus(requestId: Long, level: ApprovalLevel, status: WorkflowStatus): Boolean


    @Query("SELECT a.requestId FROM ApprovalWorkflow a WHERE a.level = 'SDM' AND a.status = 'APPROVED'")
    fun findAllApprovedRequestIdsBySdm(): List<Long>

    fun deleteByRequestId(requestId: Long)





    @Query("SELECT a.requestId FROM ApprovalWorkflow a WHERE a.approverId = :sdmEmpId AND a.level = 'SDM' AND a.status = 'APPROVED'")
    fun findApprovedRequestIdsBySdm(sdmEmpId: Long): List<Long>

    @Query("SELECT a FROM ApprovalWorkflow a WHERE a.level = 'HR_MANAGER' AND a.requestId IN :requestIds")
    fun findHrApprovalsByRequestIds( requestIds: List<Long>): List<ApprovalWorkflow>


    @Query("SELECT a FROM ApprovalWorkflow a WHERE a.requestId IN :requestIds AND a.level = 'SDM'")
    fun findSdmApprovalsByRequestIds(requestIds: List<Long>): List<ApprovalWorkflow>

}