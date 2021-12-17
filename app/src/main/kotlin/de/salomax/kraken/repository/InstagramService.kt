package de.salomax.kraken.repository

import android.net.Uri
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelError
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.moshi.moshiDeserializerOf
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import de.salomax.kraken.model.Post
import de.salomax.kraken.model.User
import java.util.*

object InstagramService {

    /**
     * Get all info about a post/reel via the Instagram api
     */
    fun getPost(shortcode: String, endpoint: String): Pair<Response, com.github.kittinunf.result.Result<Post, FuelError>> {
        val moshi = Moshi.Builder()
            .add(PostAdapter())
            .add(Date::class.java, UnixTimestampDateJsonAdapter())
            .add(Uri::class.java, UriStringJsonAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(Post::class.java)
        val (_, response, result) = Fuel
            .get("https://www.instagram.com/$endpoint/$shortcode/?__a=1")
            .responseObject(moshiDeserializerOf(moshi))

        return Pair<Response, com.github.kittinunf.result.Result<Post, FuelError>>(response, result)
    }

    fun getUser(userName: String?): Pair<Response, com.github.kittinunf.result.Result<User, FuelError>> {
        val moshi = Moshi.Builder()
            .add(UserAdapter())
            .add(Date::class.java, UnixTimestampDateJsonAdapter())
            .add(Uri::class.java, UriStringJsonAdapter())
            .build()
            .adapter(User::class.java)
        val (_, response, result) = Fuel
            .get("https://www.instagram.com/$userName/?__a=1")
            .responseObject(moshiDeserializerOf(moshi))

        return Pair<Response, com.github.kittinunf.result.Result<User, FuelError>>(response, result)
    }

}
