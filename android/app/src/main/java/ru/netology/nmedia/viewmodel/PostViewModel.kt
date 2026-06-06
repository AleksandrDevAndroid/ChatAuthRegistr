package ru.netology.nmedia.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.File

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

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImpl(AppDb.getInstance(context = application).postDao())

    val data: LiveData<FeedModel> = AppAuth.getInstance().authState
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts -> posts.map { it.copy(ownerByMe = myId == it.authorId) }}
                .map(::FeedModel)
        }.asLiveData(Dispatchers.Default)


    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData(Dispatchers.Default)
    }

    private val edited = MutableLiveData(empty)

    private val _photo = MutableLiveData<PhotoModel>(noPhoto)
    private val _photoCreated = SingleLiveEvent<Unit>()
    val photo: LiveData<PhotoModel>
        get() = _photo


    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(error = true)
        }
    }

    fun refreshPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(refreshing = true)
            repository.getAll()
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

    fun likeById(id: Long) {
        val post = data.value?.posts?.find { it.id == id }
        if (post?.likedByMe != true) {
            data.value?.let {
                viewModelScope.launch {
                    try {
                        repository.likeById(id)
                    } catch (_: Exception) {
                        _dataState.value = FeedModelState(error = true)
                    }
                }
            }

        } else
            viewModelScope.launch {
                try {
                    repository.dislikeById(id)
                } catch (_: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
    }

    fun removeById(id: Long) {
        data.value.let {
            viewModelScope.launch {
                try {
                    repository.removeById(id)
                } catch (_: Exception) {
                    _dataState.value = FeedModelState(error = true)
                }
            }
        }
    }

    fun updateStatus() {
        data.value.let {
            viewModelScope.launch {
                repository.updateStatus()
            }
        }
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.postValue(PhotoModel(uri, file))
    }
}
