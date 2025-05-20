package com.wfo_exception_tracker.wfh_exception.auth


import io.jsonwebtoken.Claims
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class JwtFilter(
    private val jwtService: JwtService
) : OncePerRequestFilter() {

    private val logger = LoggerFactory.getLogger(JwtFilter::class.java)


    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        try {
            val claims: Claims = jwtService.extractClaims(token)
            val ibsEmpId = claims.subject
            val roles = claims["roles"] as? List<*> ?: emptyList<String>()

            val authorities = roles.mapNotNull { it?.toString() }
                .map { SimpleGrantedAuthority(it) }

            val authentication = UsernamePasswordAuthenticationToken(
                ibsEmpId,
                claims,
                authorities
            )
            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
            e.printStackTrace()
            logger.warn("JWT authentication failed: ${e.message}")
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized access")
            return
        }

        filterChain.doFilter(request, response)
    }
}