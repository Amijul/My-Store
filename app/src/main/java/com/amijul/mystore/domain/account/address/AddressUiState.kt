package com.amijul.mystore.domain.account.address


data class AddressUiState(
    val fullName: String = "",
    val phone: String = "",
    val line1: String = "",
    val line2: String = "",
    val city: String = "",
    val state: String = "",
    val pincode: String = "",
    val isDefault: Boolean = false,

    // Map preview (optional for now)
    val mapLabel: String = "Map preview",
    val isLocating: Boolean = false,
    val error: String? = null
)
