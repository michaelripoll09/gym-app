package com.gymapp.profile

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/me/training-profile")
class ProfileController(private val profiles: ProfileService) {
    @PutMapping
    fun save(@RequestAttribute("authenticatedUserId") userId: UUID, @RequestBody request: TrainingProfileRequest) = profiles.save(userId, request)

    @GetMapping
    fun get(@RequestAttribute("authenticatedUserId") userId: UUID): ResponseEntity<Any> {
        val profile = profiles.get(userId) ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).body(emptyMap<String, String>())
        return ResponseEntity.status(HttpStatus.OK).body(profile)
    }

    @ExceptionHandler(ProfileValidationException::class)
    fun invalidProfile() = ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build<Void>()
}
