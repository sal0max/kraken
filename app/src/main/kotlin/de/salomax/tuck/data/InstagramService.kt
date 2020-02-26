package de.salomax.tuck.data

import android.net.Uri
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.squareup.moshi.Moshi
import java.util.*

object InstagramService {

    /**
     * Get all info about a post via the Instagram api
     */
    fun getPost(shortcode: String?): Pair<Response, com.github.kittinunf.result.Result<Post, FuelError>> {
        val moshi = Moshi.Builder()
            .add(PostAdapter())
            .add(Date::class.java, UnixTimestampDateJsonAdapter())
            .add(Uri::class.java, UriStringJsonAdapter())
            .build()
            .adapter(Post::class.java)
        val (_, response, result) = Fuel
            .get("https://www.instagram.com/p/$shortcode/?__a=1")
            .responseObject(moshiDeserializerOf(moshi))

        return Pair<Response, com.github.kittinunf.result.Result<Post, FuelError>>(response, result)
    }

}
