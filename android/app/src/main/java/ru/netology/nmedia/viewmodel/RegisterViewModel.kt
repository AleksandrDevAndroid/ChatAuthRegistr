package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.model.FeedModelAuth
import ru.netology.nmedia.repository.AuthRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val repository: AuthRepository,
    appAuth: AppAuth
) :
    ViewModel() {
    val data = appAuth.authState.asLiveData()
    val authenticated: Boolean
        get() = !data.value?.token.isNullOrEmpty()


    private val _state = MutableLiveData<FeedModelAuth>()
    val dataState: LiveData<FeedModelAuth>
        get() = _state

    fun signUp(login: String, pass: String?, name: String?, media: File?) {
        data.value.let {
            viewModelScope.launch {
                try {
                    repository.singUp(login, pass, name, media)
                    _state.value = FeedModelAuth(successes = true)
                } catch (_: Exception) {
                    _state.value = FeedModelAuth(error = true)
                }
            }
        }
    }
}