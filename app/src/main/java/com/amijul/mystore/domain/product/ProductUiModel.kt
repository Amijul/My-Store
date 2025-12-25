package com.amijul.mystore.domain.product

// Simple UI model for now (later move to domain/model)
data class ProductUiModel(
    val id: String,
    val name: String,
    val description: String = "",
    val price: Float,
    val mrp: Float = 0f,
    val unit: String = "",
    val stockQty: Int = 0,
    val imageUrl: String = "",
    val inStock: Boolean = true
)
