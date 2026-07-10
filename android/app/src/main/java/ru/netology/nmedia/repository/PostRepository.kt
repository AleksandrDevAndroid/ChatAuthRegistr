package ru.netology.nmedia.repository

import androidx.paging.Pager
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.io.File

interface PostRepository {
    suspend fun updateStatus()
    val data: Flow<PagingData<FeedItem>>
    suspend fun save(post: Post)
    suspend fun removeById(id: Long)
    suspend fun likeById(id: Long)
    suspend fun dislikeById(id: Long)
    suspend fun saveWithAttachment(post: Post, file: File)

}

