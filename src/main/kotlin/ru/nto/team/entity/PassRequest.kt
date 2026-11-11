package ru.nto.team.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "pass_request")
open class PassRequest(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = 0,

    @field:ManyToOne(fetch = FetchType.LAZY, optional = false)
    @field:JoinColumn(name = "host_id", nullable = false)
    open var host: Staff,

    @field:Column(name = "full_name", nullable = false, length = 150)
    open var fullName: String,

    @field:Column(name = "phone", nullable = false, length = 12)
    open var phone: String,

    @field:Column(name = "organization", length = 150)
    open var organization: String?,

    @field:Column(name = "purpose", nullable = false, length = 200)
    open var purpose: String,

    @field:Column(name = "valid_from", nullable = false)
    open var validFrom: LocalDateTime,

    @field:Column(name = "valid_to", nullable = false)
    open var validTo: LocalDateTime,

    @field:Enumerated(EnumType.STRING)
    @field:Column(name = "status", nullable = false, length = 20)
    open var status: RequestStatus,

    @field:Column(name = "comment", length = 500)
    open var comment: String?,

    @field:Column(name = "created_at", nullable = false, updatable = false)
    open var createdAt: LocalDateTime,

    @field:Column(name = "updated_at", nullable = false)
    open var updatedAt: LocalDateTime,
)
