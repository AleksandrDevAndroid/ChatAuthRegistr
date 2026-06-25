package ru.netology.nmedia.dto

import com.google.gson.annotations.Expose
import ru.netology.nmedia.enum.AttachmentType

data class Post(
    val id: Long,
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
)

data class Attachment(val url: String, val type: AttachmentType)
data class Media(val id: String)

