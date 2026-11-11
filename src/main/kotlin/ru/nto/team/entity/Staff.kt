package ru.nto.team.entity

import jakarta.persistence.*


@Entity
@Table(name = "staff")
open class Staff(
    @field:Id
    @field:GeneratedValue(strategy = GenerationType.IDENTITY)
    open var id: Long = 0,

    @field:Column(name = "code", nullable = false, unique = true, length = 4)
    open var code: String = "",

    @field:Column(name = "full_name", nullable = false, length = 150)
    open var fullName: String = "",
)
