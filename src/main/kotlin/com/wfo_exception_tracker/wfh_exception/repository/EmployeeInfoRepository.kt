package com.wfo_exception_tracker.wfh_exception.repository

import com.wfo_exception_tracker.wfh_exception.entity.EmployeeInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EmployeeInfoRepository : JpaRepository<EmployeeInfo, Long> {

    fun findByIbsEmpId(ibsEmpId: Long): EmployeeInfo?

    fun findUserNameByIbsEmpId(ibsEmpId: Long): String?

}
