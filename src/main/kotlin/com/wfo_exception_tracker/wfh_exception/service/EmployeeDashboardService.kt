package com.wfo_exception_tracker.wfh_exception.service

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dtos.WfhCalendarEntryForEmployee
import com.wfo_exception_tracker.wfh_exception.entity.WfhRequest
import com.wfo_exception_tracker.wfh_exception.repository.WfhRequestRepository
import org.springframework.stereotype.Service
import java.time.DayOfWeek

@Service
class EmployeeDashboardService(
    private val wfhRequestRepository: WfhRequestRepository
) {

    fun getRequestHistory(empId: Long): List<WfhRequest> =
        wfhRequestRepository.findByIbsEmpId(empId)

    fun getCalendarData(currentUserId: Long): List<WfhCalendarEntryForEmployee> {
        val requests = wfhRequestRepository.findByIbsEmpId(currentUserId)

        if (requests.isEmpty()) {
            return emptyList()
        }

        return requests.flatMap { request ->
            val dates = request.requestedStartDate
                .datesUntil(request.requestedEndDate.plusDays(1))
                .filter { date ->
                    val dayOfWeek = date.dayOfWeek
                    dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY
                }
                .toList()

            dates.map { date ->
                WfhCalendarEntryForEmployee(
                    date = date,
                    status = request.status.name,
                    reason = request.employeeReason,
                    priorityLevel = request.priority,
                    categoryOfReason = request.categoryOfReason
                )
            }
        }.sortedBy { it.date }
    }



}