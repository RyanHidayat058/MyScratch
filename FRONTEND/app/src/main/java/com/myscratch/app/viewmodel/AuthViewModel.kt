package com.myscratch.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myscratch.app.domain.model.User
import com.myscratch.app.domain.repository.AuthRepository
import com.myscratch.app.domain.repository.AuthResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false,
    val isOtpRequired: Boolean = false,
    val pendingOtpEmail: String? = null,
    val otpInfoMessage: String? = null
)

class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.currentUser.collect { user ->
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isSuccess = user != null
                )
            }
        }
    }

    fun register(name: String, email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = authRepository.registerRequest(name, email, pass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        isOtpRequired = false
                    )
                }
                is AuthResult.OtpRequired -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isOtpRequired = true,
                        pendingOtpEmail = res.email,
                        otpInfoMessage = res.message
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
                }
            }
        }
    }

    fun verifyRegisterOtp(email: String, otp: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = authRepository.verifyRegisterOtp(email, otp)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        isOtpRequired = false,
                        pendingOtpEmail = null
                    )
                    onSuccess()
                }
                is AuthResult.OtpRequired -> {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
                }
            }
        }
    }

    fun resendRegisterOtp(email: String, onResult: (Boolean, String) -> Unit = { _, _ -> }) {
        viewModelScope.launch {
            val res = authRepository.resendRegisterOtp(email)
            res.onSuccess { msg ->
                onResult(true, msg)
            }.onFailure { e ->
                onResult(false, e.localizedMessage ?: "Gagal mengirim ulang OTP.")
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val res = authRepository.login(email, pass)) {
                is AuthResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        isOtpRequired = false
                    )
                }
                is AuthResult.OtpRequired -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isOtpRequired = true,
                        pendingOtpEmail = res.email,
                        otpInfoMessage = res.message
                    )
                }
                is AuthResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = res.message)
                }
            }
        }
    }

    fun loginWithGoogle(idToken: String, name: String, email: String, photoUrl: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                errorMessage = "Silakan login menggunakan email & kata sandi akun MyScratch."
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.value = AuthUiState()
        }
    }

    fun requestChangePasswordOtp(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = authRepository.changePasswordRequest()
            _uiState.value = _uiState.value.copy(isLoading = false)
            res.onSuccess { msg -> onResult(true, msg) }
               .onFailure { e -> onResult(false, e.localizedMessage ?: "Gagal mengirim OTP.") }
        }
    }

    fun verifyChangePassword(oldPass: String, newPass: String, otp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = authRepository.changePasswordVerify(oldPass, newPass, otp)
            _uiState.value = _uiState.value.copy(isLoading = false)
            res.onSuccess { msg -> onResult(true, msg) }
               .onFailure { e -> onResult(false, e.localizedMessage ?: "Gagal mengubah kata sandi.") }
        }
    }

    fun requestChangeEmailOtp(newEmail: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = authRepository.changeEmailRequest(newEmail)
            _uiState.value = _uiState.value.copy(isLoading = false)
            res.onSuccess { msg -> onResult(true, msg) }
               .onFailure { e -> onResult(false, e.localizedMessage ?: "Gagal mengirim OTP ke email baru.") }
        }
    }

    fun verifyChangeEmail(newEmail: String, otp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = authRepository.changeEmailVerify(newEmail, otp)
            _uiState.value = _uiState.value.copy(isLoading = false)
            res.onSuccess { msg -> onResult(true, msg) }
               .onFailure { e -> onResult(false, e.localizedMessage ?: "Gagal mengubah email.") }
        }
    }

    fun deleteAccount(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val res = authRepository.deleteAccount()
            _uiState.value = AuthUiState()
            res.onSuccess { msg -> onResult(true, msg) }
               .onFailure { e -> onResult(false, e.localizedMessage ?: "Gagal menghapus akun.") }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun clearOtpState() {
        _uiState.value = _uiState.value.copy(isOtpRequired = false, pendingOtpEmail = null, otpInfoMessage = null)
    }

    companion object {
        fun provideFactory(authRepository: AuthRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AuthViewModel(authRepository) as T
                }
            }
    }
}
