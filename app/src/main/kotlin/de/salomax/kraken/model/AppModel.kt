package de.salomax.kraken.model

import android.net.Uri
import com.squareup.moshi.JsonClass
import java.util.*

/*
 * own, nice classes
 */

data class Post(
    val id: Long,
    val shortcode: String,
    val dateTime: Date,
    val owner: Owner,
    val images: List<Image>
)

@JsonClass(generateAdapter = true)
data class Owner(
    val id: Long,
    val username: String
)

data class Image(
    val id: Long,
    val shortcode: String,
    val imageUrl: Uri,
    val isVideo: Boolean,
    val videoUrl: Uri?,
    val contentDescription: String?
)

data class User(
    val id: Long,
    val username: String,
    val fullName: String?,
    val biography: String?,
    val isPrivate: Boolean,
    val followedBy: Long,
    val follows: Long,
    val isJoinedRecently: Boolean,
    val isBusinessAccount: Boolean,
    val isVerified: Boolean,
    val profilePicUrl: String,
    val profilePicUrlHd: String
)
