package ru.netology.nmedia.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.LoadType.*
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import kotlinx.coroutines.CancellationException
import ru.netology.nmedia.api.PostsApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.entity.toEntity
import ru.netology.nmedia.error.ApiError

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val service: PostsApiService,
    private val dao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>
    ): MediatorResult {

        try {
            val response = when (loadType) {
                REFRESH -> {
                    val id = postRemoteKeyDao.max()
                    if (id != null && id != 0L) {
                        service.getAfter(id, state.config.pageSize)
                    } else service.getLatest(state.config.pageSize)
                }

                PREPEND -> {
                    val id = postRemoteKeyDao.max()
                    service.getBefore(id, state.config.pageSize)
                }

                APPEND -> {
                    val id = postRemoteKeyDao.min()
                    service.getBefore(id, state.config.pageSize)
                }
            }

            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(
                response.code(),
                response.message(),
            )

            appDb.withTransaction {
                when (loadType) {
                    REFRESH -> {
                        if (postRemoteKeyDao.max() == null) {
                            postRemoteKeyDao.inset(
                                listOf(
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.AFTER,
                                        body.first().id
                                    ),
                                    PostRemoteKeyEntity(
                                        PostRemoteKeyEntity.KeyType.BEFORE,
                                        body.last().id
                                    )
                                )
                            )
                        } else postRemoteKeyDao.inset(
                            PostRemoteKeyEntity(
                                PostRemoteKeyEntity.KeyType.BEFORE,
                                body.first().id
                            )
                        )
                    }

                    PREPEND -> {}

                    APPEND -> {
                        postRemoteKeyDao.inset(
                            PostRemoteKeyEntity(PostRemoteKeyEntity.KeyType.AFTER, body.last().id)
                        )
                    }
                }
            }

            dao.insert(body.toEntity())
            return MediatorResult.Success(body.isEmpty())

        } catch (e: Exception) {
            if (e is CancellationException) {
                throw e
            }
            return MediatorResult.Error(e)
        }
    }
}