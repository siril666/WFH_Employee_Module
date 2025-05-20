package com.wfo_exception_tracker.wfh_exception.repository

// --- EmployeeMasterRepository.kt ---

import com.wfo_exception_tracker.wfh_exception.entity.EmployeeMaster
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmployeeMasterRepository : JpaRepository<EmployeeMaster, Long> {

    fun findByIbsEmpId(empId: Long): EmployeeMaster?

    fun findByDmId(dmId: Long): List<EmployeeMaster>

    fun existsByTeamOwnerIdAndDmId(teamOwnerId: Long, sdmId: Long): Boolean

}
