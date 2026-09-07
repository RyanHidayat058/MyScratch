package com.myscratch.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequestDto(
    val name: String,
    val email: String,
    val password: String
)

data class VerifyRegisterOtpDto(
    val email: String,
    val otp: String
)

data class ResendRegisterOtpDto(
    val email: String
)

data class LoginRequestDto(
    val email: String,
    val password: String
)

data class BaseResponseDto(
    val success: Boolean,
    val message: String? = null
)

data class AuthResponseDto(
    val success: Boolean,
    val message: String? = null,
    val token: String? = null,
    val user: UserDto? = null
)

data class UserDto(
    val id: Any,
    val name: String,
    val email: String,
    @SerializedName("email_verified_at")
    val emailVerifiedAt: String? = null
)
