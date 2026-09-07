package com.myscratch.app.data.network

import android.content.Context
import android.content.SharedPreferences
import com.myscratch.app.domain.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TokenManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("myscratch_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<User?>(getUser())
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun saveAuth(token: String, user: User) {
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .apply()
        _currentUser.value = user
    }

    fun getUser(): User? {
        val id = prefs.getString(KEY_USER_ID, null) ?: return null
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        return User(id = id, name = name, email = email)
    }

    fun updateUserData(name: String? = null, email: String? = null) {
        val current = _currentUser.value ?: return
        val updated = current.copy(
            name = name ?: current.name,
            email = email ?: current.email
        )
        prefs.edit()
            .putString(KEY_USER_NAME, updated.name)
            .putString(KEY_USER_EMAIL, updated.email)
            .apply()
        _currentUser.value = updated
    }

    fun clear() {
        prefs.edit().clear().apply()
        _currentUser.value = null
    }

    fun isLoggedIn(): Boolean {
        return getToken() != null
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
    }
}
