package com.shoesmarket.infra.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "product")
class ProductEntity(
    name: String,
    brand: String,
    price: Long,
) : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Column(name = "name", nullable = false, length = 200)
    var name: String = name
        protected set

    @Column(name = "brand", nullable = false, length = 100)
    var brand: String = brand
        protected set

    @Column(name = "price", nullable = false)
    var price: Long = price
        protected set

    fun changePrice(newPrice: Long) {
        require(newPrice >= 0) { "가격은 0 이상이어야 합니다." }
        this.price = newPrice
    }
}
