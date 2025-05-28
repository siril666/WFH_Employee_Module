package com.wfo_exception_tracker.wfh_exception.service


import com.wfo_exception_tracker.wfh_exception.dtos.*
import com.wfo_exception_tracker.wfh_exception.repository.ApprovalWorkflowRepository
import com.wfo_exception_tracker.wfh_exception.repository.EmployeeInfoRepository
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek
import java.time.LocalDate

@Service
class TeamManagerService(
    private val wfhRequestRepository: WfhRequestRepository,
    private val approvalWorkflowRepository: ApprovalWorkflowRepository,
    private val employeeInfoRepository: EmployeeInfoRepository
) {

//    fun getRequestsForTeamManager(teamOwnerId: Long): List<WfhRequestForTm> {
//
//        return wfhRequestRepository.findByTeamOwnerId(teamOwnerId).map { request ->
//            val tmApproval = approvalWorkflowRepository.findByRequestIdAndLevel(request.requestId, ApprovalLevel.TEAM_MANAGER)
//            val sdmApproval = approvalWorkflowRepository.findByRequestIdAndLevel(request.requestId, ApprovalLevel.SDM)
//            val employeeInfo = employeeInfoRepository.findByIbsEmpId(request.ibsEmpId)
//            WfhRequestForTm(
//                employeeName = employeeInfo?.userName,
//                request = request,
//                tmStatus = tmApproval?.status?.name ?: "PENDING",
//                sdmStatus = sdmApproval?.status?.name,
//                tmActionDate = tmApproval?.updatedDate,
//                sdmActionDate = sdmApproval?.updatedDate
//            )
//        }


    fun getRequestsForTeamManager(teamOwnerId: Long): List<WfhRequestForTm> {
        return wfhRequestRepository.findByTeamOwnerId(teamOwnerId).map { request ->
            val tmApproval = approvalWorkflowRepository.findByRequestIdAndLevel(request.requestId, ApprovalLevel.TEAM_MANAGER)
            val sdmApproval = approvalWorkflowRepository.findByRequestIdAndLevel(request.requestId, ApprovalLevel.SDM)
            val hrApproval = approvalWorkflowRepository.findByRequestIdAndLevel(request.requestId, ApprovalLevel.HR_MANAGER)
            val employeeInfo = employeeInfoRepository.findByIbsEmpId(request.ibsEmpId)
            WfhRequestForTm(
                employeeName = employeeInfo?.userName,
                request = request,
                tmStatus = tmApproval?.status?.name ?: "PENDING",
                sdmStatus = sdmApproval?.status?.name,
                hrStatus = hrApproval?.status?.name,
                tmActionDate = tmApproval?.updatedDate,
                sdmActionDate = sdmApproval?.updatedDate,
                hrActionDate = hrApproval?.updatedDate
            )
        }
    }

//    fun getWFHCalendarData(teamOwnerId: Long): Map<String, Int> {
//        val approvedRequests = wfhRequestRepository.findByTeamOwnerId(teamOwnerId)
//
//        val dateMap = mutableMapOf<String, Int>()
//        for (req in approvedRequests) {
//            var date = req.requestedStartDate
//            while (!date.isAfter(req.requestedEndDate)) {
//                val dayOfWeek = date.dayOfWeek
//                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
//                    val dateStr = date.toString()  // Format: yyyy-MM-dd
//                    dateMap[dateStr] = dateMap.getOrDefault(dateStr, 0) + 1
//                }
//                date = date.plusDays(1)
//            }
//        }
//        return dateMap
//    }



    fun  getTeamManagerCalendar(teamOwnerId: Long): List<TeamManagerCalendarDay> {
        // 1. Get all requests for this team
        val requests = wfhRequestRepository.findByTeamOwnerId(teamOwnerId)
        val requestIds = requests.map { it.requestId }

        // 2. Get all relevant approvals in one query
        val approvals = approvalWorkflowRepository.findByRequestIdIn(requestIds)

        // 3. Build calendar with detailed status breakdown
        val calendarMap = mutableMapOf<LocalDate, TeamManagerDayStatus>()

        requests.forEach { request ->
            val requestApprovals = approvals.filter { it.requestId == request.requestId }
            val teamLeadApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.TEAM_MANAGER}
            val sdmApproval = requestApprovals.firstOrNull { it.level == ApprovalLevel.SDM }

            val status = when {
                teamLeadApproval != null -> teamLeadApproval.status.name to (sdmApproval?.status?.name)
                else -> "PENDING" to null
            }

            request.requestedStartDate.datesUntil(request.requestedEndDate.plusDays(1))
                .filter { date ->
                    // Exclude weekends
                    date.dayOfWeek.value < 6
                }
                .forEach { date ->
                    val currentStatus = calendarMap.getOrPut(date) {
                        TeamManagerDayStatus(0, 0, 0, 0)
                    }

                    calendarMap[date] = when (status.first) {
                        "APPROVED" -> {
                            if (status.second == null) {
                                currentStatus.copy(pendingWithSdm = currentStatus.pendingWithSdm + 1)
                            } else {
                                currentStatus.copy(approvedByTeamLead = currentStatus.approvedByTeamLead + 1)
                            }
                        }
                        "REJECTED" -> {
                            currentStatus.copy(rejectedByTeamLead = currentStatus.rejectedByTeamLead + 1)
                        }
                        else -> {
                            currentStatus.copy(pendingWithTeamLead = currentStatus.pendingWithTeamLead + 1)
                        }
                    }
                }
        }

        return calendarMap.map { (date, status) ->
            TeamManagerCalendarDay(date, status)
        }.sortedBy { it.date }
    }

    fun getApprovedEmployeesByDate(teamOwnerId: Long, date: LocalDate): List<Map<String, Any>> {
        val dayOfWeek = date.dayOfWeek
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return emptyList() // No WFH on weekends
        }

        return wfhRequestRepository.findByTeamOwnerId(teamOwnerId)
            .filter {
                it.status == RequestStatus.APPROVED &&
                        !date.isBefore(it.requestedStartDate) &&
                        !date.isAfter(it.requestedEndDate)
            }
            .map {
                val empName = employeeInfoRepository.findByIbsEmpId(it.ibsEmpId)?.userName ?: "N/A"
                mapOf(
                    "ibsEmpId" to it.ibsEmpId,
                    "employeeName" to empName,
                    "from" to it.requestedStartDate,
                    "to" to it.requestedEndDate
                )
            }
    }

}