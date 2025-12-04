package com.amijul.mystore.domain.home


// Simple UI model for now (we'll later move this to domain layer)
data class StoreUiModel(
    val id: String,
    val name: String,
    val category: String,
    val distanceText: String,   // e.g. "250m away"
    val imageUrl: String,
    val locationName: String,
    val isOpen: Boolean
)

