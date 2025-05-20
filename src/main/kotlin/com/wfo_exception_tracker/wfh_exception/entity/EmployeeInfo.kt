package com.wfo_exception_tracker.wfh_exception.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor


@Entity(name = "employee_info")
@Data
@NoArgsConstructor
@AllArgsConstructor
data class EmployeeInfo(
    @Id
    @Column(name = "ibs_emp_id")
    var ibsEmpId: Long,

    @field:NotBlank
    @Column(name = "user_name")
    val userName: String,

    @field:NotBlank
    val role: String,

    @field:Email
    @field:NotBlank
    @Column(name = "email_id", unique = true)
    val emailId: String,

    @Column(name = "phone_number")
    val phoneNumber: Long,

    val location: String,

    val country: String,

    @Column(name = "admin_verified")
    val adminVerified: Boolean = false
)
