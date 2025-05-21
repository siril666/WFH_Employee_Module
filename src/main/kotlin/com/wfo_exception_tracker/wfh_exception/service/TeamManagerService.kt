package com.wfo_exception_tracker.wfh_exception.service


import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import com.wfo_exception_tracker.wfh_exception.dtos.WfhRequestForTm
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

    fun getRequestsForTeamManager(teamOwnerId: Long): List<WfhRequestForTm> {

        return wfhRequestRepository.findByTeamOwnerId(teamOwnerId).map { request ->
            val tmApproval = approvalWorkflowRepository.findByRequestIdAndLevel(request.requestId, ApprovalLevel.TEAM_MANAGER)
            val sdmApproval = approvalWorkflowRepository.findByRequestIdAndLevel(request.requestId, ApprovalLevel.SDM)
            val employeeInfo = employeeInfoRepository.findByIbsEmpId(request.ibsEmpId)
            WfhRequestForTm(
                employeeName = employeeInfo?.userName,
                request = request,
                tmStatus = tmApproval?.status?.name ?: "PENDING",
                sdmStatus = sdmApproval?.status?.name,
                tmActionDate = tmApproval?.updatedDate,
                sdmActionDate = sdmApproval?.updatedDate
            )
        }
    }

    fun getWFHCalendarData(teamOwnerId: Long): Map<String, Int> {
        val approvedRequests = wfhRequestRepository.findByTeamOwnerId(teamOwnerId)
            .filter { it.status == RequestStatus.APPROVED }

        val dateMap = mutableMapOf<String, Int>()
        for (req in approvedRequests) {
            var date = req.requestedStartDate
            while (!date.isAfter(req.requestedEndDate)) {
                val dayOfWeek = date.dayOfWeek
                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    val dateStr = date.toString()  // Format: yyyy-MM-dd
                    dateMap[dateStr] = dateMap.getOrDefault(dateStr, 0) + 1
                }
                date = date.plusDays(1)
            }
        }
        return dateMap
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