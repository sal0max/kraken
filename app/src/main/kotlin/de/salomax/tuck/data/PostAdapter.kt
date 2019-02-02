package de.salomax.tuck.data

import com.squareup.moshi.FromJson

internal class PostAdapter {

    @FromJson
    @Suppress("unused")
    fun fromJson(json: Result): Post {
        return Post(
            json.graphQl.shortcodeMedia.id,
            json.graphQl.shortcodeMedia.shortCode,
            json.graphQl.shortcodeMedia.takenAtTimestamp,
            json.graphQl.shortcodeMedia.owner,
            // carousel
            if (json.graphQl.shortcodeMedia.edgeSidecarToChildren != null) {
                json.graphQl.shortcodeMedia.edgeSidecarToChildren.edges
                    .map {
                        Image(
                            it.node.id,
                            it.node.shortCode,
                            it.node.displayUrl,
                            it.node.isVideo,
                            it.node.videoUrl,
                            it.node.accessibilityCaption
                        )
                    }
            }
            // single image
            else {
                listOf(
                    Image(
                        json.graphQl.shortcodeMedia.id,
                        json.graphQl.shortcodeMedia.shortCode,
                        json.graphQl.shortcodeMedia.displayUrl,
                        json.graphQl.shortcodeMedia.isVideo,
                        json.graphQl.shortcodeMedia.videoUrl,
                        json.graphQl.shortcodeMedia.accessibilityCaption
                    )
                )
            }
        )
    }

}

