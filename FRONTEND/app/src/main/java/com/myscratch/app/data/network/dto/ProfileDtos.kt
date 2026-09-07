package com.myscratch.app.data.network.dto

import com.google.gson.annotations.SerializedName

data class ChangePasswordVerifyDto(
    @SerializedName("old_password")
    val oldPassword: String,
    @SerializedName("new_password")
    val newPassword: String,
    val otp: String
)

data class ChangeEmailRequestDto(
    @SerializedName("new_email")
    val newEmail: String
)

data class ChangeEmailVerifyDto(
    @SerializedName("new_email")
    val newEmail: String,
    val otp: String
)

data class ProfileResponseDto(
    val success: Boolean,
    val user: UserDto? = null,
    val message: String? = null
)
