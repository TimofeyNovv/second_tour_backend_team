package ru.nto.team.validation

import ru.nto.team.CreateRequestBody
import ru.nto.team.exception.FieldValidationException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.ResolverStyle
import java.util.Locale
import ru.nto.team.DecisionBody
import ru.nto.team.entity.RequestStatus

data class ValidatedCreateRequest(
    val fullName: String,
    val phone: String,
    val organization: String?,
    val purpose: String,
    val validFrom: LocalDateTime,
    val validTo: LocalDateTime,
)

data class ValidatedDecision(val status: RequestStatus, val comment: String?)

object RequestValidation {

    private val phonePattern = Regex("\\+7[0-9]{10}")

    private val datePattern = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}")

    private val dateFormatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ss", Locale.ROOT)
        .withResolverStyle(ResolverStyle.STRICT)

    fun validateCreate(body: CreateRequestBody, now: LocalDateTime): ValidatedCreateRequest {
        val errors = linkedMapOf<String, String>()

        val fullName = body.fullName?.trim().orEmpty()
        val phone = body.phone?.trim().orEmpty()
        val organization = body.organization?.trim()?.takeIf { it.isNotEmpty() }
        val purpose = body.purpose?.trim().orEmpty()

        if (fullName.length !in 5..150) {
            errors["fullName"] = "ФИО должно содержать от 5 до 150 символов"
        }

        if (!phonePattern.matches(phone)) {
            errors["phone"] = "Телефон должен содержать +7 и десять цифр"
        }

        if (organization != null && organization.length > 150) {
            errors["organization"] = "Организация должна содержать не более 150 символов"
        }

        if (purpose.length !in 5..200) {
            errors["purpose"] = "Цель должна содержать от 5 до 200 символов"
        }

        val validFrom = parseDate(value = body.validFrom?.trim(), field = "validFrom", errors = errors)

        val validTo = parseDate(value = body.validTo?.trim(), field = "validTo", errors = errors)

        if (validFrom != null && validFrom.isBefore(now)) {
            errors["validFrom"] = "Начало посещения не должно быть раньше текущего времени"
        }

        if (validFrom != null && validTo != null) {
            if (!validTo.isAfter(validFrom) || validTo.toLocalDate() != validFrom.toLocalDate()) {
                errors["validTo"] = "Окончание должно быть позже начала и в тот же день"
            }
        }

        if (errors.isNotEmpty()) {
            throw FieldValidationException(errors)
        }

        return ValidatedCreateRequest(
            fullName = fullName,
            phone = phone,
            organization = organization,
            purpose = purpose,
            validFrom = requireNotNull(validFrom),
            validTo = requireNotNull(validTo),
        )
    }

    fun validateDecision(body: DecisionBody): ValidatedDecision {
        val errors = linkedMapOf<String, String>()

        val status = when (body.status?.trim()) {
            "APPROVED" -> RequestStatus.APPROVED
            "REJECTED" -> RequestStatus.REJECTED
            else -> null
        }

        val comment = body.comment?.trim()

        if (status == null) {
            errors["status"] = "Допустимы только APPROVED и REJECTED"
        }

        if (status == RequestStatus.REJECTED) {
            if (comment.isNullOrEmpty() || comment.length > 500) {
                errors["comment"] = "Причина отклонения должна содержать от 1 до 500 символов"
            }
        }

        if (errors.isNotEmpty()) {
            throw FieldValidationException(errors)
        }

        return ValidatedDecision(
            status = requireNotNull(status),
            comment = if (status == RequestStatus.REJECTED) {
                comment
            } else {
                null
            },
        )
    }

    private fun parseDate(value: String?, field: String, errors: MutableMap<String, String>): LocalDateTime? {
        if (value.isNullOrEmpty()) {
            errors[field] = "Поле обязательно"
            return null
        }

        if (!datePattern.matches(value)) {
            errors[field] = "Ожидается дата в формате yyyy-MM-dd'T'HH:mm:ss"
            return null
        }

        return try {
            LocalDateTime.parse(value, dateFormatter)
        } catch (_: DateTimeParseException) {
            errors[field] = "Некорректная дата или время"
            null
        }
    }
}
