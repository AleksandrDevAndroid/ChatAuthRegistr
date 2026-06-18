package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.enum.AttachmentType

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val authorId: Long,
    val author: String,
    val authorAvatar: String?,
    val content: String,
    val published: Long,
    val likedByMe: Boolean,
    val likes: Int = 0,
    val status: Boolean,
    @Embedded
    val attachment: AttachmentEmbeddable?
) {
    fun toDto() = Post(
        id,
        authorId,
        author,
        authorAvatar,
        content,
        published,
        likedByMe,
        likes,
        status,
        attachment?.toDto(),
        false
    )

    companion object {
        fun fromDto(dto: Post, status: Boolean = true) =
            PostEntity(
                dto.id,
                dto.authorId,
                dto.author,
                dto.authorAvatar,
                dto.content,
                dto.published,
                dto.likedByMe,
                dto.likes,
                status = status,
                AttachmentEmbeddable.fromDto(dto.attachment)
            )
    }
}

    data class AttachmentEmbeddable(
        var url: String,
        var type: AttachmentType,
    ) {
        fun toDto() = Attachment(url, type)

        companion object {
            fun fromDto(dto: Attachment?) = dto?.let {
                AttachmentEmbeddable(it.url, it.type)
            }
        }
    }

fun List<PostEntity>.toDto(): List<Post> = this.map { it.toDto() }
fun List<Post>.toEntity(status: Boolean = true): List<PostEntity> = this.map { PostEntity.fromDto(it, status) }
