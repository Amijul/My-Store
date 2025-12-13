package com.amijul.mystore.domain.account

import androidx.compose.runtime.Immutable

@Immutable
data class AccountUi(
    val name: String,
    val email: String,
    val photoUrl: String? = null
)
