package com.amijul.mystore.domain.auth

interface RoleRepository {
    suspend fun ensureBuyerRole()
    suspend fun setRoleAfterSignup(role: String)

}