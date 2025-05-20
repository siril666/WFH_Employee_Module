package com.wfo_exception_tracker.wfh_exception.repository

import com.wfo_exception_tracker.wfh_exception.dtos.PriorityLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface WfhRequestRepository : JpaRepository<WfhRequest, Long> {

    fun findByIbsEmpId(ibsEmpId: Long): List<WfhRequest>
    fun findByTeamOwnerId(teamOwnerId: Long): List<WfhRequest>
    fun findByRequestId(requestId: Long): WfhRequest?
    fun findByRequestIdIn(requestIds: List<Long>): List<WfhRequest>
    fun findByTeamOwnerIdIn(teamOwnerId: List<Long>): List<WfhRequest>

    @Query("""
        SELECT r FROM WfhRequest r
        WHERE r.teamOwnerId = :teamOwnerId
        AND r.requestedEndDate >= :requestedStartDate
        AND r.requestedStartDate <= :requestedEndDate
    """)
    fun findByTeamOwnerIdAndDateRange(
         teamOwnerId: Long,
         requestedStartDate: LocalDate,
         requestedEndDate: LocalDate
    ): List<WfhRequest>

    @Query("""
    SELECT r FROM WfhRequest r
    WHERE r.teamOwnerId IN :teamOwnerIds
    AND r.requestedEndDate >= :requestedStartDate
    AND r.requestedStartDate <= :requestedEndDate
""")
    fun findByTeamOwnerIdInAndDateRange(
        teamOwnerIds: List<Long>,
        requestedStartDate: LocalDate,
        requestedEndDate: LocalDate
    ): List<WfhRequest>





    // Find all by current status (for example: PENDING, APPROVED, etc.)
    fun findByStatus(status: RequestStatus): List<WfhRequest>

    // Find all by priority
    fun findByPriority(priority: PriorityLevel): List<WfhRequest>

    // Find all by team owner

    // Custom: Get WFH requests for a list of request IDs (used by SDM view)

    // Optional: Get all pending requests assigned to a specific DM
    fun findByDmIdAndStatus(dmId: Long, status: RequestStatus): List<WfhRequest>

    @Query("SELECT w FROM WfhRequest w WHERE w.requestId IN :requestIds AND w.dmId = :dmId")
    fun findApprovedRequestsForSdm(requestIds: List<Long>, dmId: Long): List<WfhRequest>


    fun findByTeamOwnerIdAndStatus(teamOwnerId: Long, status: RequestStatus): List<WfhRequest>
}
