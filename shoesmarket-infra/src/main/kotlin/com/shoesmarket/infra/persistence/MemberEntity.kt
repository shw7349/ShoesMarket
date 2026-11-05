package com.shoesmarket.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "member",
    uniqueConstraints = [UniqueConstraint(name = "uk_member_email", columnNames = ["email"])],
)
class MemberEntity(
    email: String,
    password: String,
    displayName: String,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "email", nullable = false, length = 200)
    var email: String = email
        protected set

    @Column(name = "password", nullable = false, length = 200)
    var password: String = password
        protected set

    @Column(name = "display_name", nullable = false, length = 100)
    var displayName: String = displayName
        protected set
}
