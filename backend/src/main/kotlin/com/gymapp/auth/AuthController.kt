package com.gymapp.auth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@RestController
@RequestMapping("/api/v1")
class AuthController(private val auth: AuthService) {
    @PostMapping("/auth/register")
    fun register(@RequestBody request: RegisterRequest) = ResponseEntity.status(HttpStatus.CREATED).body(auth.register(request))

    @PostMapping("/auth/login")
    fun login(@RequestBody request: LoginRequest) = auth.login(request)

    @GetMapping("/me")
    fun me(@RequestAttribute("authenticatedUserId") userId: UUID) = auth.me(userId)

    @ExceptionHandler(DuplicateEmailException::class)
    fun duplicateEmail() = ResponseEntity.status(HttpStatus.CONFLICT).build<Void>()

    @ExceptionHandler(InvalidCredentialsException::class)
    fun invalidCredentials() = ResponseEntity.status(HttpStatus.UNAUTHORIZED).build<Void>()
}

@Component
class BearerTokenFilter(private val jwt: JwtService) : OncePerRequestFilter() {
    override fun shouldNotFilter(request: HttpServletRequest) = !request.requestURI.startsWith("/api/v1/me") && !request.requestURI.startsWith("/api/v1/workout-plans") && !request.requestURI.startsWith("/api/v1/workout-sessions") && !request.requestURI.startsWith("/api/v1/curated-plans") && !request.requestURI.startsWith("/api/v1/training-summary")

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val token = request.getHeader("Authorization")?.removePrefix("Bearer ")
        val userId = token?.let(jwt::subject)
        if (userId == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value())
            return
        }
        request.setAttribute("authenticatedUserId", userId)
        filterChain.doFilter(request, response)
    }
}
