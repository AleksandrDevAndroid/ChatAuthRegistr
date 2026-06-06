package ru.netology.nmedia.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.FeedModelAuth
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.PostRepositoryImpl


class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val repository: AuthRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())
    val data = AppAuth.getInstance().authState.asLiveData()
    val authenticated: Boolean
        get() = !data.value?.token.isNullOrEmpty()

    private val _state = MutableLiveData<FeedModelAuth>()
    val dataState: LiveData<FeedModelAuth>
        get() = _state

    fun signIn(login: String, pass: String?) {
        data.value.let {
            viewModelScope.launch {
                try {
                    repository.singIn(login, pass)
                    _state.value = FeedModelAuth(successes = true)
                } catch (_: Exception) {
                    _state.value = FeedModelAuth(error = true)
                }
            }
        }
    }
}


