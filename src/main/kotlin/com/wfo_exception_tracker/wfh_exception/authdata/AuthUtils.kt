package com.wfo_exception_tracker.wfh_exception.authdata

import io.jsonwebtoken.Claims
import org.springframework.security.core.context.SecurityContextHolder

object AuthUtils {
    fun getCurrentUserId(): Long {
        val authentication = SecurityContextHolder.getContext().authentication
        val claims = authentication.credentials as Claims
        return claims.subject.toLong()
    }
}