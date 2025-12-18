package com.amijul.mystore.domain.auth

interface RoleRepository {
    suspend fun ensureBuyerRole()
}