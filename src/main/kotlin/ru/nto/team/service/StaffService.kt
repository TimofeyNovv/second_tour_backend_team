package ru.nto.team.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.nto.team.dto.ProfileDto
import ru.nto.team.entity.Staff
import ru.nto.team.exception.ApiException
import ru.nto.team.repository.StaffRepository

@Service
@Transactional(readOnly = true)
class StaffService(private val staffRepository: StaffRepository) {
    private val codePattern = Regex("[A-Za-z0-9]{4}")

    fun profile(code: String): ProfileDto {
        val staff = requireStaff(code)
        return ProfileDto(name = staff.fullName)
    }

    fun requireStaff(code: String): Staff {
        if (!codePattern.matches(code)) {
            throw ApiException(400, "Invalid code format")
        }

        return staffRepository.findByCode(code) ?: throw ApiException(401, "Unauthorized")
    }
}
