package com.wfo_exception_tracker.wfh_exception.auth


import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureException
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import javax.crypto.SecretKey

@Component
class JwtService {

    @Value("\${app.security.jwt.key}")
    private lateinit var secret: String


    private fun getKey(): SecretKey {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
    }


    fun extractClaims(token: String): Claims {
        return try {
            Jwts.parser()
                .verifyWith(getKey())
                .build()
                .parseSignedClaims(token)
                .payload
        } catch (e: SignatureException) {
            throw RuntimeException("Invalid JWT signature.")
        } catch (e: Exception) {
            e.printStackTrace()
            throw RuntimeException(e.message)
        }
    }
}
