package ru.nto.team.service

import org.springframework.stereotype.Service
import ru.nto.team.dto.ProfileDto
import ru.nto.team.exception.ApiException

@Service
class StaffService {
    fun profile(code: String): ProfileDto = throw ApiException(501, "Task not implemented")
}
