package com.myscratch.app.domain.repository

import com.myscratch.app.domain.model.User
import kotlinx.coroutines.flow.StateFlow

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class OtpRequired(val email: String, val message: String) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

interface AuthRepository {
    val currentUser: StateFlow<User?>

    suspend fun registerRequest(name: String, email: String, password: String): AuthResult
    suspend fun verifyRegisterOtp(email: String, otp: String): AuthResult
    suspend fun resendRegisterOtp(email: String): Result<String>
    suspend fun login(email: String, password: String): AuthResult
    suspend fun logout()
    suspend fun changePasswordRequest(): Result<String>
    suspend fun changePasswordVerify(oldPassword: String, newPassword: String, otp: String): Result<String>
    suspend fun changeEmailRequest(newEmail: String): Result<String>
    suspend fun changeEmailVerify(newEmail: String, otp: String): Result<String>
    suspend fun deleteAccount(): Result<String>
    fun isUserLoggedIn(): Boolean
}
