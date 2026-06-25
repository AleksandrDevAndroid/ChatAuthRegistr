package ru.netology.nmedia.auth

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.Firebase
import com.google.firebase.messaging.messaging
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuth @Inject constructor(
    @ApplicationContext
   private val context: Context
) {

    private val KEY_ID = "id"
    private val KEY_TOKEN = "token"
    val pref = context.getSharedPreferences(("auth"), (Context.MODE_PRIVATE))
    private val _authStateFlow = MutableStateFlow(AuthState())

    val authState: StateFlow<AuthState>
        get() = _authStateFlow

    init {
        val id = pref.getLong(KEY_ID, 0)
        val token = pref.getString(KEY_TOKEN, null)

        if (id != 0L && !token.isNullOrEmpty()) {
            _authStateFlow.value = AuthState(id, token)
        }
        sendPushToken()
    }

    fun setAuth(id: Long, token: String?) {
        _authStateFlow.value = AuthState(id, token)
        pref.edit() {
            putLong(KEY_ID, id)
            putString(KEY_TOKEN, token)
        }
        Log.e("myID", "ID:${authState.value.id}")

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


    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AppAuthEntryPoint {
        fun getPostApiService(): PostsApiService
    }

    fun sendPushToken(token: String? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            runCatching {
              val entryPoint =  EntryPointAccessors.fromApplication(context, AppAuthEntryPoint::class.java)
                entryPoint.getPostApiService().pushToken(
                    PushToken(
                        token ?: Firebase.messaging.token.await()
                    )
                )
            }
        }
    }

}

data class AuthState(val id: Long = 0, val token: String? = null)