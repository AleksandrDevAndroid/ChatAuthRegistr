package ru.netology.nmedia.dto

import ru.netology.nmedia.enum.AttachmentType

sealed interface FeedItem{
    val id : Long
}
data class Post(
    override val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val status: Boolean,
    val attachment: Attachment? = null,
    val ownerByMe: Boolean
) : FeedItem

data class Attachment(val url: String, val type: AttachmentType)
data class Media(val id: String)

data class Ad(override val id: Long, val image: String) : FeedItem

data class DateSeparator(override val id: Long, val text : String) : FeedItem
