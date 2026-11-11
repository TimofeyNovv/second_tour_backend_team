package ru.nto.team.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nto.team.CreateRequestBody
import ru.nto.team.DecisionBody
import ru.nto.team.dto.RequestDetailsDto
import ru.nto.team.dto.RequestListDto
import ru.nto.team.dto.toDetailsDto
import ru.nto.team.dto.toListDto
import ru.nto.team.entity.RequestStatus
import ru.nto.team.exception.ApiException
import ru.nto.team.repository.PassRequestRepository
import ru.nto.team.entity.PassRequest
import ru.nto.team.validation.RequestValidation
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit

@Service
class RequestService(
    private val staffService: StaffService,
    private val requestRepository: PassRequestRepository,
    private val clock: Clock,
) {
    @Transactional(readOnly = true)
    fun list(code: String, status: String?): List<RequestListDto> {
        val staff = staffService.requireStaff(code)

        val filter = if (status == null) {
            null
        } else {
            RequestStatus.entries.firstOrNull { it.name == status }
                ?: throw ApiException(400, "Invalid status")
        }

        val requests = if (filter == null) {
            requestRepository.findAllByHost_IdOrderByValidFromAscIdAsc(hostId = staff.id)
        } else {
            requestRepository.findAllByHost_IdAndStatusOrderByValidFromAscIdAsc(hostId = staff.id, status = filter)
        }

        return requests.map { it.toListDto() }
    }

    @Transactional(readOnly = true)
    fun details(code: String, id: Long): RequestDetailsDto {
        val staff = staffService.requireStaff(code)

        val request = requestRepository.findByIdAndHost_Id(id = id, hostId = staff.id)
            ?: throw ApiException(404, "Request not found")

        return request.toDetailsDto()
    }

    @Transactional
    fun create(code: String, body: CreateRequestBody): RequestDetailsDto {
        val staff = staffService.requireStaff(code)
        val now = LocalDateTime.ofInstant(clock.instant(), ZoneId.of("Europe/Moscow"))
        val validated = RequestValidation.validateCreate(body = body, now = now)

        val duplicateExists = requestRepository.existsOverlappingRequest(
            hostId = staff.id,
            phone = validated.phone,
            statuses = listOf(RequestStatus.PENDING, RequestStatus.APPROVED),
            validFrom = validated.validFrom,
            validTo = validated.validTo,
        )
        if (duplicateExists) throw ApiException(409, "Duplicate request")
        val timestamp = now.truncatedTo(ChronoUnit.SECONDS)
        val request = PassRequest(
            host = staff,
            fullName = validated.fullName,
            phone = validated.phone,
            organization = validated.organization,
            purpose = validated.purpose,
            validFrom = validated.validFrom,
            validTo = validated.validTo,
            status = RequestStatus.PENDING,
            comment = null,
            createdAt = timestamp,
            updatedAt = timestamp,
        )

        return requestRepository.save(request).toDetailsDto()
    }

    @Transactional
    fun cancel(code: String, id: Long): RequestDetailsDto {
        val staff = staffService.requireStaff(code)
        val request = requestRepository.findByIdAndHost_Id(id = id, hostId = staff.id)
            ?: throw ApiException(404, "Request not found")

        if (request.status != RequestStatus.PENDING) throw ApiException(409, "Invalid status transition")
        val timestamp = LocalDateTime.ofInstant(clock.instant(), ZoneId.of("Europe/Moscow"))
            .truncatedTo(ChronoUnit.SECONDS)

        request.status = RequestStatus.CANCELLED
        request.updatedAt = timestamp
        return requestRepository.save(request).toDetailsDto()
    }

    @Transactional
    fun decide(id: Long, body: DecisionBody): RequestDetailsDto {
        val request = requestRepository.findById(id).orElseThrow { ApiException(404, "Request not found") }

        val validated = RequestValidation.validateDecision(body)
        if (request.status != RequestStatus.PENDING) {
            throw ApiException(409, "Invalid status transition")
        }

        val timestamp = LocalDateTime.ofInstant(clock.instant(), ZoneId.of("Europe/Moscow"))
            .truncatedTo(ChronoUnit.SECONDS)

        request.status = validated.status
        request.comment = validated.comment
        request.updatedAt = timestamp
        return requestRepository.save(request).toDetailsDto()
    }
}
