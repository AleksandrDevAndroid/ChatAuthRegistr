package ru.netology.nmedia.auth

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dto.PushToken

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
        sendPushToken()
    }

    fun setAuth(id: Long ,token: String?){
        _authStateFlow.value = AuthState(id,token)
        pref.edit(){
            putLong(KEY_ID,id)
            putString(KEY_TOKEN,token)
        }
        sendPushToken()
    }

    fun removeAuth() {
        _authStateFlow.value = AuthState()
        with(pref.edit()) {
            clear()
            commit()
        }
        sendPushToken()
    }

    fun sendPushToken(token : String? = null){
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
                PostsApi.service.pushToken(PushToken(token ?: Firebase.messaging.token.await()))
            }
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