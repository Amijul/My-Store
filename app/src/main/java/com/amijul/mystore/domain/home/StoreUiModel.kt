package com.amijul.mystore.domain.home


// Simple UI model for now (we'll later move this to domain layer)
data class StoreUiModel(
    val id: String,
    val name: String,
    val type: String,
    val phone: String,
    val imageUrl: String,

    // Address
    val line1: String,
    val city: String,
    val state: String,
    val pincode: String,

    // Status
    val isActive: Boolean
)

