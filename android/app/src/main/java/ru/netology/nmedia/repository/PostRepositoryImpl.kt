package ru.netology.nmedia.repository

import  androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.api.*
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.enum.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.File
import java.io.IOException
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val dao: PostDao,
    private val apiService: PostsApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) :
    PostRepository, AuthRepository {
    @Inject
    lateinit var appAuth: AppAuth

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 10, enablePlaceholders = false),
        pagingSourceFactory = { dao.getPagingSource() },
        remoteMediator = PostRemoteMediator(apiService, dao, postRemoteKeyDao, appDb)
    ).flow
        .map { pagingData ->
            pagingData.map {
                it.toDto()
            }
        }

    override suspend fun updateStatus() {
        dao.updateStatus()
    }


    override suspend fun save(post: Post) {
        try {
            val response = apiService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body, status = true))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) {
        val oldPost = dao.getId(id)
        dao.removeById(oldPost.id)
        try {
            val response = apiService.removeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            dao.insert(PostEntity.fromDto(oldPost.toDto()))
            throw NetworkError
        } catch (e: Exception) {
            dao.insert(PostEntity.fromDto(oldPost.toDto()))
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        val oldPost = dao.getId(id)
        val newPost = oldPost.copy(likedByMe = true, likes = +1)
        dao.insert(PostEntity.fromDto(newPost.toDto(), status = true))
        try {
            val response = apiService.likeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body, status = true))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun dislikeById(id: Long) {
        val oldPost = dao.getId(id)
        val newPost = oldPost.copy(likedByMe = false, likes = 0)
        dao.insert(PostEntity.fromDto(newPost.toDto(), status = true))
        try {
            val response = apiService.dislikeById(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromDto(body, status = true))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWithAttachment(post: Post, file: File) {
        val media = upload(file)
        val copyPost = post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
        save(copyPost)

    }

    private suspend fun upload(file: File?): Media {
        try {
            val part = MultipartBody.Part.createFormData(
                "file", file!!.name,
                file.asRequestBody()
            )
            val response = apiService.upload(part)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            return response.body() ?: throw ApiError(response.code(), response.message())

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun singIn(login: String, pass: String?) {
        try {
            val response = apiService.singIn(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun singUp(
        login: String,
        pass: String?,
        name: String?,
        media: File?
    ) {
        try {
            val part = if (media != null && media.exists()) {
                MultipartBody.Part.createFormData("file", media!!.name, media.asRequestBody())
            } else null
            val nameReg = name?.toRequestBody("text/plain".toMediaTypeOrNull())
            val passReg = pass?.toRequestBody("text/plain".toMediaTypeOrNull())
            val loginReg = login.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = apiService.singUpWithPhoto(loginReg, passReg, nameReg, part)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
            appAuth.setAuth(body.id, body.token)

        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}

