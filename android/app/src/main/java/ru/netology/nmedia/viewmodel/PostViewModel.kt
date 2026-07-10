package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File
import javax.inject.Inject

private val empty = Post(
    id = 0,
    authorId = 0,
    author = "",
    authorAvatar = "",
    content = "",
    published = 0,
    likedByMe = false,
    likes = 0,
    status = false,
    ownerByMe = false
)

private val noPhoto = PhotoModel()

@HiltViewModel
class PostViewModel @Inject constructor(private val repository: PostRepository, appAuth: AppAuth) :
    ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val data: Flow<PagingData<FeedItem>> = appAuth.authState
        .flatMapLatest { (myId, _) ->
            repository.data.map { posts ->
                posts.map { post ->
                    if (post is Post) {
                        post.copy(ownerByMe = myId == post.authorId)
                    } else {
                        post
                    }
                }
            }
        }.flowOn(Dispatchers.Default)
        .cachedIn(viewModelScope)


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(empty)
    private val _photo = MutableLiveData<PhotoModel>(noPhoto)
    private val _photoCreated = SingleLiveEvent<Unit>()
    val photo: LiveData<PhotoModel>
        get() = _photo


    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated


    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun save() {
        edited.value?.let {
            _postCreated.value = Unit
            viewModelScope.launch {
                try {
                    when (_photo.value) {
                        noPhoto -> repository.save(it)
                        else -> _photo.value?.file?.let { file ->
                            repository.saveWithAttachment(it, file)
                        }
                    }
                    _dataState.value = FeedModelState()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
        edited.value = empty
        _photo.value = noPhoto
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun likeById(id: Long, likedByMe: Boolean) {
        viewModelScope.launch {
            try {
                if (!likedByMe) {
                    repository.likeById(id)
                } else repository.dislikeById(id)
            } catch (e: Exception) {
                FeedModelState(error = true)
            }
        }
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
            } catch (_: Exception) {
                _dataState.value = FeedModelState(error = true)
            }
        }
    }


    fun getNewPost() {
        viewModelScope.launch {
            val id = repository
        }
    }


    fun updateStatus() {
        viewModelScope.launch {
            repository.updateStatus()
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.postValue(PhotoModel(uri, file))
    }
}
