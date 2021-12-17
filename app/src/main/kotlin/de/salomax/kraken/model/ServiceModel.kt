package de.salomax.kraken.model

import android.net.Uri
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.*

/*
 * garbage (graphql) from API
 */

@JsonClass(generateAdapter = true)
data class Result(
    @field:Json(name = "graphql") val graphQl: GraphQl
)

@JsonClass(generateAdapter = true)
data class GraphQl(
    @field:Json(name = "shortcode_media") val shortcodeMedia: ShortcodeMedia?,
    @field:Json(name = "user") val user: GraphUser?
)

@JsonClass(generateAdapter = true)
data class ShortcodeMedia(
    @field:Json(name = "id") val id: Long,
    @field:Json(name = "shortcode") val shortCode: String,
    @field:Json(name = "display_url") val displayUrl: Uri,
    @field:Json(name = "owner") val owner: Owner,
    @field:Json(name = "is_video") val isVideo: Boolean,
    @field:Json(name = "video_url") val videoUrl: Uri?,
    @field:Json(name = "edge_sidecar_to_children") val edgeSidecarToChildren: EdgeSidecarToChildren?,
    @field:Json(name = "taken_at_timestamp") val takenAtTimestamp: Date,
    @field:Json(name = "accessibility_caption") val accessibilityCaption: String?
)

@JsonClass(generateAdapter = true)
data class EdgeSidecarToChildren(
    @field:Json(name = "edges") val edges: List<Edge>
)

@JsonClass(generateAdapter = true)
data class Edge(
    @field:Json(name = "node") val node: Node
)

@JsonClass(generateAdapter = true)
data class Node(
    @field:Json(name = "id") val id: Long,
    @field:Json(name = "shortcode") val shortCode: String,
    @field:Json(name = "display_url") val displayUrl: Uri,
    @field:Json(name = "is_video") val isVideo: Boolean,
    @field:Json(name = "video_url") val videoUrl: Uri?,
    @field:Json(name = "accessibility_caption") val accessibilityCaption: String
)

@JsonClass(generateAdapter = true)
data class GraphUser(
    @field:Json(name = "biography") val biography: String?,
    @field:Json(name = "edge_followed_by") val followedBy: Count,
    @field:Json(name = "edge_follow") val follows: Count,
    @field:Json(name = "full_name") val fullName: String?,
    @field:Json(name = "id") val id: Long,
    @field:Json(name = "is_joined_recently") val isJoinedRecently: Boolean,
    @field:Json(name = "is_business_account") val isBusinessAccount: Boolean,
    @field:Json(name = "is_private") val isPrivate: Boolean,
    @field:Json(name = "is_verified") val isVerified: Boolean,
    @field:Json(name = "profile_pic_url") val profilePicUrl: String,
    @field:Json(name = "profile_pic_url_hd") val profilePicUrlHd: String,
    @field:Json(name = "username") val username: String
)

@JsonClass(generateAdapter = true)
data class Count(
    @field:Json(name = "count") val count: Long
)
