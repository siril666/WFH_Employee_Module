package com.wfo_exception_tracker.wfh_exception.entity


import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.WorkflowStatus
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
@Table(name = "approval_workflow")
data class ApprovalWorkflow(

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    val workflowId: Long = 0,

    @Column(nullable = false)
    val requestId: Long,  // FK to WfhRequest

    @Column(nullable = false)
    val approverId: Long,  // FK to EmployeeMaster

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val level: ApprovalLevel,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: WorkflowStatus,

    @Column(nullable = false)
    val updatedBy: String,

    @Column(nullable = false)
    val updatedDate: LocalDate
)
