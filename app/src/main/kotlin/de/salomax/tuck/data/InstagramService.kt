package de.salomax.tuck.data

import android.net.Uri
import com.squareup.moshi.*
import io.reactivex.Observable
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.*

interface InstagramService {

    companion object {
        fun create(): InstagramService {

            val moshi = Moshi.Builder()
                .add(PostAdapter())
                .add(Date::class.java, UnixTimestampDateJsonAdapter())
                .add(Uri::class.java, UriStringJsonAdapter())
                .build()

            val retrofit = Retrofit.Builder()
                .addCallAdapterFactory(
                    RxJava2CallAdapterFactory.create()
                )
                .addConverterFactory(
                    MoshiConverterFactory.create(moshi)
                )
                .baseUrl("https://www.instagram.com/")
                .build()

            return retrofit.create(InstagramService::class.java)
        }
    }

    @GET("p/{shortcode}/?__a=1")
    fun getPost(
        @Path("shortcode") shortcode: String
    ): Observable<Post>

}

