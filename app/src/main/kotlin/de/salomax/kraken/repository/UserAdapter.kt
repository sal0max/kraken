package de.salomax.kraken.repository

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import de.salomax.kraken.model.User
import de.salomax.kraken.model.Result
import java.lang.UnsupportedOperationException

internal class UserAdapter {

    @FromJson
    @Suppress("unused")
    fun fromJson(json: Result): User? {
        if (json.graphQl.user == null) {
            return null
        }
        return User(
            json.graphQl.user.id,
            json.graphQl.user.username,
            json.graphQl.user.fullName,
            json.graphQl.user.biography,
            json.graphQl.user.isPrivate,
            json.graphQl.user.followedBy.count,
            json.graphQl.user.follows.count,
            json.graphQl.user.isJoinedRecently,
            json.graphQl.user.isBusinessAccount,
            json.graphQl.user.isVerified,
            json.graphQl.user.profilePicUrl,
            json.graphQl.user.profilePicUrlHd
        )
    }

    @Suppress("unused", "UNUSED_PARAMETER")
    @ToJson
    fun toJson(user: User): Result?  {
        throw UnsupportedOperationException()
    }

}
