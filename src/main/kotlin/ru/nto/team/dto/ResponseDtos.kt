package ru.nto.team.dto

import ru.nto.team.entity.PassRequest
import ru.nto.team.entity.RequestStatus
import java.time.LocalDateTime

data class ProfileDto(val name: String)

data class RequestListDto(
    val id: Long,
    val fullName: String,
    val validFrom: LocalDateTime,
    val validTo: LocalDateTime,
    val status: RequestStatus,
)

data class RequestDetailsDto(
    val id: Long,
    val fullName: String,
    val phone: String,
    val organization: String?,
    val purpose: String,
    val validFrom: LocalDateTime,
    val validTo: LocalDateTime,
    val status: RequestStatus,
    val comment: String?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
)

data class ErrorDto(val error: String)

data class ValidationErrorsDto(val errors: Map<String, String>)

fun PassRequest.toListDto(): RequestListDto = RequestListDto(
    id = id,
    fullName = fullName,
    validFrom = validFrom,
    validTo = validTo,
    status = status,
)

fun PassRequest.toDetailsDto(): RequestDetailsDto = RequestDetailsDto(
    id = id,
    fullName = fullName,
    phone = phone,
    organization = organization,
    purpose = purpose,
    validFrom = validFrom,
    validTo = validTo,
    status = status,
    comment = comment,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
