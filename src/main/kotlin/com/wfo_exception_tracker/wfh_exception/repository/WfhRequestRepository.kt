package com.wfo_exception_tracker.wfh_exception.repository

import com.wfo_exception_tracker.wfh_exception.dtos.PriorityLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import com.wfo_exception_tracker.wfh_exception.dto.GraphReportView
import com.wfo_exception_tracker.wfh_exception.dto.WfhRequestReportView

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




   // @Query("SELECT r FROM WfhRequest r WHERE r.requestId IN :requestIds")
  //  fun findByRequestIdIn(@Param("requestIds") requestIds: List<Long>): List<WfhRequest>




    @Query(
        nativeQuery = true,
        value = """
    SELECT r.*
    FROM wfh_request r
    INNER JOIN employee_info e ON r.ibs_emp_id = e.ibs_emp_id
    WHERE r.request_id IN (
        SELECT DISTINCT request_id
        FROM approval_workflow
        WHERE (level = 'SDM' AND status = 'APPROVED')
           OR (level = 'HR_MANAGER' AND status IN ('APPROVED', 'REJECTED'))
    )
    AND (:empId IS NULL OR r.ibs_emp_id = :empId)
    AND (:empName IS NULL OR LOWER(e.user_name) LIKE LOWER(CONCAT('%', :empName, '%')))
    AND (
        :date IS NULL OR 
        (r.requested_start_date <= :date AND r.requested_end_date >= :date)
    )
     AND (:categoryOfReason IS NULL OR LOWER(r.category_of_reason) = LOWER(:categoryOfReason))
    """
    )
    fun findFilteredApprovedRequests(
        @Param("empId") empId: Long?,
        @Param("empName") empName: String?,
        @Param("date") date: LocalDate?,
        @Param("categoryOfReason") categoryOfReason: String?

    ): List<WfhRequest>


    @Query(
        nativeQuery = true,
        value = """
    SELECT r.request_id AS requestId,
           r.ibs_emp_id AS ibsEmpId,
           e.user_name AS employeeName,
           m.team AS team,
           r.requested_start_date AS requestedStartDate,
           r.requested_end_date AS requestedEndDate,
           r.category_of_reason AS categoryOfReason,
           r.priority AS priority,
           r.status AS status
    FROM wfh_request r
    INNER JOIN employee_info e ON r.ibs_emp_id = e.ibs_emp_id
    INNER JOIN employee_master m ON r.ibs_emp_id = m.ibs_emp_id
    WHERE r.request_id IN (
        SELECT DISTINCT request_id
        FROM approval_workflow
        WHERE (level = 'SDM' AND status = 'APPROVED')
           OR (level = 'HR_MANAGER' AND status IN ('APPROVED', 'REJECTED'))
    )
    AND (:teamName IS NULL OR LOWER(m.team) LIKE LOWER(CONCAT('%', :teamName, '%')))
    AND (
        CAST(:date AS DATE) IS NULL OR 
        (r.requested_start_date <= CAST(:date AS DATE) AND r.requested_end_date >= CAST(:date AS DATE))
    )
    """
    )
    fun findApprovedRequestsByTeamAndDate(
        @Param("teamName") teamName: String?,
        @Param("date") date: LocalDate?
    ): List<WfhRequestReportView>



    @Query(
        nativeQuery = true,
        value = """
    SELECT 
      EXTRACT(YEAR FROM r.requested_start_date) AS year,
      CASE 
        WHEN :period = 'WEEK' THEN EXTRACT(WEEK FROM r.requested_start_date)
        WHEN :period = 'MONTH' THEN EXTRACT(MONTH FROM r.requested_start_date)
        WHEN :period = 'QUARTER' THEN EXTRACT(QUARTER FROM r.requested_start_date)
      END AS period_value,
      m.team AS team,
      r.status AS status,                     -- Add status here
      COUNT(*) AS request_count
    FROM wfh_request r
    JOIN employee_master m ON r.ibs_emp_id = m.ibs_emp_id
    WHERE r.request_id IN (
        SELECT DISTINCT request_id
        FROM approval_workflow
        WHERE (level = 'SDM' AND status = 'APPROVED')
           OR (level = 'HR_MANAGER' AND status IN ('APPROVED', 'REJECTED'))
    )
    AND (:teamName IS NULL OR LOWER(m.team) LIKE LOWER(CONCAT('%', :teamName, '%')))
    GROUP BY year, period_value, m.team, r.status          -- Add r.status here too
    ORDER BY year, period_value, r.status
    """
    )
    fun getRequestsReport(
        @Param("period") period: String,
        @Param("teamName") teamName: String?
    ): List<GraphReportView>


}
