package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AppAuth(private val context: Context) {
    val pref = context.getSharedPreferences(("auth"), (Context.MODE_PRIVATE))
    private val _authStateFlow = MutableStateFlow(AuthState())

    val authState: StateFlow<AuthState>
        get() = _authStateFlow

    init {
        val id = pref.getLong(KEY_ID, 0)
        val token = pref.getString(KEY_TOKEN, null)

        if (id != 0L && !token.isNullOrEmpty()) {
            _authStateFlow.value = AuthState(id,token)
        }
    }

    fun setAuth(id: Long ,token: String?){
        _authStateFlow.value = AuthState(id,token)
        pref.edit(){
            putLong(KEY_ID,id)
            putString(KEY_TOKEN,token)
        }
    }

    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(pref.edit()) {
            clear()
            commit()
        }
    }

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_TOKEN = "token"
        private var INSTANCE: AppAuth? = null
        fun getInstance() = INSTANCE ?: throw RuntimeException("call init first")
        fun init(context: Context) {
            INSTANCE = AppAuth(context)
        }
    }
}

data class AuthState(val id: Long = 0, val token: String? = null)