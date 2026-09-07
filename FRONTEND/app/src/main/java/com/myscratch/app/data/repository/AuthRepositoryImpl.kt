package com.myscratch.app.data.repository

import com.google.gson.Gson
import com.myscratch.app.data.network.ApiClient
import com.myscratch.app.data.network.TokenManager
import com.myscratch.app.data.network.dto.*
import com.myscratch.app.domain.model.User
import com.myscratch.app.domain.repository.AuthRepository
import com.myscratch.app.domain.repository.AuthResult
import kotlinx.coroutines.flow.StateFlow

class AuthRepositoryImpl(
    private val tokenManager: TokenManager
) : AuthRepository {

    private val apiService = ApiClient.getService(tokenManager)
    private val gson = Gson()

    override val currentUser: StateFlow<User?> = tokenManager.currentUser

    override suspend fun registerRequest(name: String, email: String, password: String): AuthResult {
        return try {
            val response = apiService.registerRequest(RegisterRequestDto(name, email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                AuthResult.OtpRequired(
                    email = email,
                    message = response.body()?.message ?: "Kode verifikasi telah dikirim ke email Anda."
                )
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                AuthResult.Error(errorMsg ?: "Pendaftaran gagal. Silakan periksa kembali data Anda.")
            }
        } catch (e: Exception) {
            AuthResult.Error("Tidak dapat terhubung ke server: ${e.localizedMessage}")
        }
    }

    override suspend fun verifyRegisterOtp(email: String, otp: String): AuthResult {
        return try {
            val response = apiService.verifyRegisterOtp(VerifyRegisterOtpDto(email, otp))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val userDto = body.user!!
                val user = User(
                    id = userDto.id.toString(),
                    name = userDto.name,
                    email = userDto.email
                )
                tokenManager.saveAuth(body.token ?: "", user)
                AuthResult.Success(user)
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                AuthResult.Error(errorMsg ?: "Kode OTP salah atau kedaluwarsa.")
            }
        } catch (e: Exception) {
            AuthResult.Error("Gagal verifikasi OTP: ${e.localizedMessage}")
        }
    }

    override suspend fun resendRegisterOtp(email: String): Result<String> {
        return try {
            val response = apiService.resendRegisterOtp(ResendRegisterOtpDto(email))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Kode verifikasi baru telah dikirim.")
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg ?: "Gagal mengirim ulang OTP."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(email: String, password: String): AuthResult {
        return try {
            val response = apiService.login(LoginRequestDto(email, password))
            if (response.isSuccessful && response.body()?.success == true) {
                val body = response.body()!!
                val userDto = body.user!!
                val user = User(
                    id = userDto.id.toString(),
                    name = userDto.name,
                    email = userDto.email
                )
                tokenManager.saveAuth(body.token ?: "", user)
                AuthResult.Success(user)
            } else {
                val rawError = response.errorBody()?.string()
                val errorMsg = parseErrorMessage(rawError)
                if (response.code() == 403 && rawError?.contains("email_not_verified") == true) {
                    AuthResult.OtpRequired(
                        email = email,
                        message = errorMsg ?: "Email belum diverifikasi. Silakan masukkan kode OTP."
                    )
                } else {
                    AuthResult.Error(errorMsg ?: "Email atau kata sandi tidak cocok.")
                }
            }
        } catch (e: Exception) {
            AuthResult.Error("Gagal masuk: ${e.localizedMessage}")
        }
    }

    override suspend fun logout() {
        try {
            apiService.logout()
        } catch (_: Exception) {}
        tokenManager.clear()
    }

    override suspend fun changePasswordRequest(): Result<String> {
        return try {
            val response = apiService.changePasswordRequest()
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Kode OTP telah dikirim ke email Anda.")
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg ?: "Gagal meminta kode OTP ganti kata sandi."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changePasswordVerify(oldPassword: String, newPassword: String, otp: String): Result<String> {
        return try {
            val response = apiService.changePasswordVerify(ChangePasswordVerifyDto(oldPassword, newPassword, otp))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Kata sandi berhasil diperbarui.")
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg ?: "Gagal memperbarui kata sandi."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeEmailRequest(newEmail: String): Result<String> {
        return try {
            val response = apiService.changeEmailRequest(ChangeEmailRequestDto(newEmail))
            if (response.isSuccessful && response.body()?.success == true) {
                Result.success(response.body()?.message ?: "Kode OTP telah dikirim ke email baru.")
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg ?: "Gagal meminta kode OTP ganti email."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun changeEmailVerify(newEmail: String, otp: String): Result<String> {
        return try {
            val response = apiService.changeEmailVerify(ChangeEmailVerifyDto(newEmail, otp))
            if (response.isSuccessful && response.body()?.success == true) {
                tokenManager.updateUserData(email = newEmail)
                Result.success(response.body()?.message ?: "Alamat email berhasil diperbarui.")
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg ?: "Gagal memperbarui alamat email."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<String> {
        return try {
            val response = apiService.deleteAccount()
            if (response.isSuccessful && response.body()?.success == true) {
                tokenManager.clear()
                Result.success(response.body()?.message ?: "Akun berhasil dihapus permanen.")
            } else {
                val errorMsg = parseErrorMessage(response.errorBody()?.string())
                Result.failure(Exception(errorMsg ?: "Gagal menghapus akun."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun isUserLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        if (errorBody.isNullOrBlank()) return null
        return try {
            val base = gson.fromJson(errorBody, BaseResponseDto::class.java)
            base.message
        } catch (_: Exception) {
            null
        }
    }
}
