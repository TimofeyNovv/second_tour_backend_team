package ru.nto.team.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import ru.nto.team.dto.ErrorDto
import ru.nto.team.dto.ValidationErrorsDto

class ApiException(
    val status: Int,
    override val message:String,
) : RuntimeException(message)

class FieldValidationException(
    val errors: Map<String, String>,
) : RuntimeException("Validation failed")

@RestControllerAdvice
class ApiExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(
        exception: ApiException,
    ): ResponseEntity<ErrorDto> = ResponseEntity
        .status(exception.status)
        .body(ErrorDto(error = exception.message))

    @ExceptionHandler(FieldValidationException::class)
    fun handleFieldValidationException(
        exception: FieldValidationException,
    ): ResponseEntity<ValidationErrorsDto> = ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ValidationErrorsDto(errors = exception.errors))
}