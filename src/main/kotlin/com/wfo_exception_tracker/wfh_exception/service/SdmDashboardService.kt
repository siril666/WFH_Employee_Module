package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.dtos.*
import com.wfo_exception_tracker.wfh_exception.exception.AccessDeniedException
import com.wfo_exception_tracker.wfh_exception.exception.ResourceNotFoundException
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeInfoRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeMasterRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class SdmDashboardService(
    private val approvalWorkflowRepository: ApprovalWorkflowRepository,
    private val wfhRequestRepository: WfhRequestRepository,
    private val employeeMasterRepository: EmployeeMasterRepository,
    private val employeeInfoRepository: EmployeeInfoRepository,
) {

    fun getRequestsForSdm(sdmEmpId: Long): List<WfhRequestForSdm> {

        // 1. Get all team owners (TMs) who report to this SDM
        val teamOwners = employeeMasterRepository.findByDmId(sdmEmpId).map { it.teamOwnerId }

        // 2. Get request IDs approved by these TMs
        val tmApprovedRequestIds = approvalWorkflowRepository.findApprovedRequestIdsByTeamManager(teamOwners)

        // 3. Get the complete WFH requests
        val wfhRequests = wfhRequestRepository.findByRequestIdIn(tmApprovedRequestIds)

        // 4. Get all relevant approvals in one query
        val allApprovals = approvalWorkflowRepository.findByRequestIdIn(tmApprovedRequestIds)

        return wfhRequests.map { request ->
            val requestApprovals = allApprovals.filter { it.requestId == request.requestId }
            WfhRequestForSdm(
                employeeName = employeeInfoRepository.findByIbsEmpId(request.ibsEmpId)?.userName ?:"unknown",
                request = request,
                sdmStatus = requestApprovals
                    .firstOrNull { it.level == ApprovalLevel.SDM }
                    ?.status?.toString() ?: "PENDING",
                hrStatus = requestApprovals
                    .firstOrNull { it.level == ApprovalLevel.HR_MANAGER }
                    ?.status?.toString(),
                sdmUpdatedDate = requestApprovals
                    .firstOrNull { it.level == ApprovalLevel.SDM }
                    ?.updatedDate,
                teamOwnerName = request.teamOwnerId?.let {
                    employeeInfoRepository.findByIbsEmpId(it)?.userName
                } ?: "Unknown",
                teamName =employeeMasterRepository.findByIbsEmpId(request.ibsEmpId)?.team
            )
        }
    }


    fun generateSdmCalendar(sdmId: Long): List<SdmCalendarDay> {
        // 1. Get teams managed by this SDM
        println(sdmId)
        val managedTeams = employeeMasterRepository.findByDmId(sdmId).map { it.teamOwnerId }
        println(managedTeams)
        // 2. Get all requests from these teams
        val requests = wfhRequestRepository.findByTeamOwnerIdIn(managedTeams)
        val requestIds = requests.map { it.requestId }

        // 3. Get all relevant approvals in one query
        val approvals = approvalWorkflowRepository.findByRequestIdIn(requestIds)

        // 4. Build calendar
        val calendarMap = mutableMapOf<LocalDate, MutableMap<Long, SdmDayStatus>>()

        requests.forEach { request ->
            val requestApprovals = approvals.filter { it.requestId == request.requestId }
            val sdmApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.SDM }
            val hrApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.HR_MANAGER }

            val status = when {
                sdmApproval != null -> sdmApproval.status.name to (hrApproval?.status?.name)
                else -> "PENDING" to null
            }

            request.requestedStartDate.datesUntil(request.requestedEndDate.plusDays(1))
                .filter { date ->
                    // Exclude Saturdays (6) and Sundays (7)
                    date.dayOfWeek.value < 6
                }
                .forEach { date ->
                    val teamMap = calendarMap.getOrPut(date) { mutableMapOf() }
                    val counts = teamMap.getOrPut(request.teamOwnerId!!) {
                        SdmDayStatus(0, 0, 0, 0)
                    }

                    when (status.first) {
                        "APPROVED" -> {
                            val newCount = if (status.second == null) {
                                counts.copy(pendingWithHr = counts.pendingWithHr + 1)
                            } else {
                                counts.copy(approvedBySdm = counts.approvedBySdm + 1)
                            }
                            teamMap[request.teamOwnerId!!] = newCount
                        }
                        "REJECTED" -> {
                            teamMap[request.teamOwnerId!!] =
                                counts.copy(rejectedBySdm = counts.rejectedBySdm + 1)
                        }
                        else -> {
                            teamMap[request.teamOwnerId!!] =
                                counts.copy(pendingWithSdm = counts.pendingWithSdm + 1)
                        }
                    }
                }
        }

        return calendarMap.map { (date, teamStatus) ->
            SdmCalendarDay(date, teamStatus)
        }.sortedBy { it.date }
    }

    fun getSdmDateDetails(date: LocalDate, sdmId: Long): List<SdmRequestDetail> {

        // Skip weekends
        if (date.dayOfWeek.value >= 6) {
            return emptyList()
        }
        // 1. Get all teamOwnerIds managed by the SDM
        val managedTeams = employeeMasterRepository.findByDmId(sdmId).map { it.teamOwnerId }

        if (managedTeams.isEmpty()) {
            throw AccessDeniedException("You don't manage any teams")
        }

        // 2. Get all requests from these teams where the date is within start and end date range
        val requests = wfhRequestRepository.findByTeamOwnerIdInAndDateRange(
            managedTeams,
            date,
            date
        )

        // 3. Get all approvals for these requests
        val approvals = approvalWorkflowRepository.findByRequestIdIn(requests.map { it.requestId })

        // 4. Build response
        return requests.map { request ->
            val requestApprovals = approvals.filter { it.requestId == request.requestId }
            val sdmApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.SDM }
            val hrApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.HR_MANAGER }

            val employee = employeeInfoRepository.findById(request.ibsEmpId)
                .orElseThrow { ResourceNotFoundException("Employee not found") }

            SdmRequestDetail(
                requestId = request.requestId,
                employeeId = request.ibsEmpId,
                employeeName = employee.userName,
                sdmStatus = sdmApproval?.status?.name ?: "PENDING",
                hrStatus = hrApproval?.status?.name,
                startDate = request.requestedStartDate,
                endDate = request.requestedEndDate,
                reason = request.employeeReason,
                teamOwnerId = request.teamOwnerId
            )
        }
    }

//    fun getSdmDateDetails(date: LocalDate, sdmId: Long, teamOwnerId: Long): List<SdmRequestDetail> {
//        // 1. Verify team belongs to this SDM
//
//        if (!employeeMasterRepository.existsByTeamOwnerIdAndDmId(teamOwnerId, sdmId)) {
//            throw AccessDeniedException("You don't manage this team")
//        }
//
//
//        // 2. Get requests for this date
//        val requests = wfhRequestRepository.findByTeamOwnerIdAndDateRange(
//            teamOwnerId,
//            date,
//            date
//        )
//
//        // 3. Get approvals in one query
//        val approvals = approvalWorkflowRepository.findByRequestIdIn(requests.map { it.requestId })
//
//        return requests.map { request ->
//            val requestApprovals = approvals.filter { it.requestId == request.requestId }
//            val sdmApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.SDM }
//            val hrApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.HR_MANAGER }
//
//            val employee = employeeInfoRepository.findById(request.ibsEmpId)
//                .orElseThrow { ResourceNotFoundException("Employee not found") }
//
//            SdmRequestDetail(
//                requestId = request.requestId,
//                employeeId = request.ibsEmpId,
//                employeeName = employee.userName,
//                sdmStatus = sdmApproval?.status?.name ?: "PENDING",
//                hrStatus = hrApproval?.status?.name,
//                startDate = request.requestedStartDate,
//                endDate = request.requestedEndDate,
//                reason = request.employeeReason
//            )
//        }
//    }
//

}