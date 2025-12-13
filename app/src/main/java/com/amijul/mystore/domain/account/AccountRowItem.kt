package com.amijul.mystore.domain.account

import androidx.compose.runtime.Immutable

@Immutable
data class AccountRowItem(
    val title: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val onClick: () -> Unit
)
