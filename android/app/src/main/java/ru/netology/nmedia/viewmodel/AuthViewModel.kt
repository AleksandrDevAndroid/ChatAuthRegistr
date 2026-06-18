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
import ru.netology.nmedia.repository.PostRepositoryImpl
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: PostRepositoryImpl,
    appAuth: AppAuth
) :
    ViewModel() {

    val data = appAuth.authState.asLiveData()
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


