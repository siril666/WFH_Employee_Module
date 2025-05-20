package com.wfo_exception_tracker.wfh_exception.entity

import com.wfo_exception_tracker.wfh_exception.dtos.PriorityLevel
import com.wfo_exception_tracker.wfh_exception.dtos.RequestStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDate

@Entity
@Table(name = "wfh_request")
data class WfhRequest(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val requestId: Long = 0,

    @Column(nullable = false)
    val ibsEmpId: Long,  // New field name

    @Column(nullable = false)
    val requestedStartDate: LocalDate,

    @Column(nullable = false)
    val requestedEndDate: LocalDate,

    @Column(columnDefinition = "TEXT", nullable = false)
    val employeeReason: String,

    @Column(nullable = false)
    val categoryOfReason: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: RequestStatus = RequestStatus.PENDING,

    val teamOwnerId: Long?,

    val dmId: Long?,

    val termDuration: String?,

    @Enumerated(EnumType.STRING)
    val priority: PriorityLevel = PriorityLevel.MODERATE,

    val currentLocation: String?,

    val attachmentPath: String?
)
