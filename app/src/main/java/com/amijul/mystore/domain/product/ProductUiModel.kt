package com.amijul.mystore.domain.product

// Simple UI model for now (later move to domain/model)
data class ProductUiModel(
    val id: String,
    val name: String,
    val price: Float,
    val unit: String,
    val imageUrl: String,
    val inStock: Boolean
)