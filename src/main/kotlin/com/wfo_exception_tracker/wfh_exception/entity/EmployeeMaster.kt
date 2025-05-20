package com.wfo_exception_tracker.wfh_exception.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table


@Entity
@Table(name = "employee_master")
data class EmployeeMaster(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val ibsEmpId: Long = 0,  // Primary Key

    @Column(nullable = false)
    val expediaFGName: String,

    val jobLevel: String,

    @Column(nullable = false)
    val role: String,

    val rate: Double?,

    val hm: String?,  // Hiring Manager

    val country: String,

    val location: String,

    val sowTeamName: String,

    val svpOrg: String,

    val vpOrg: String,

    val directorOrg: String,

    val team: String,

    val teamOwner: String,

    val teamOwnerId: Long,

    val dm: String,

    val dmId: Long,

    val billability: String,

    @Column(columnDefinition = "TEXT")
    val remarks: String? = null
)
