package com.wfo_exception_tracker.wfh_exception.controller

import com.wfo_exception_tracker.wfh_exception.authdata.AuthUtils
import com.wfo_exception_tracker.wfh_exception.dtos.ApprovalLevel
import com.wfo_exception_tracker.wfh_exception.dtos.FullEmployeeDetailsWithApproval
import com.wfo_exception_tracker.wfh_exception.entity.EmployeeInfo
import com.wfo_exception_tracker.wfh_exception.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/user")
@CrossOrigin(origins = [ "http://localhost:5173"])
class UserController(
    private val userService: UserService
) {

    @GetMapping("/profile")
    fun getProfile(): ResponseEntity<EmployeeInfo> {
        val profile = userService.getProfile(AuthUtils.getCurrentUserId())
        return profile?.let { ResponseEntity.ok(it) } ?: ResponseEntity.notFound().build()
    }

}