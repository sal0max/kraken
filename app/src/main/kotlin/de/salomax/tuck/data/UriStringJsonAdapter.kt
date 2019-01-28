package de.salomax.tuck.data

import android.net.Uri
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException
import java.util.Date

/**
 * Formats Uris.
 * To use, add this as an adapter for `Uri.class` on your [Moshi.Builder][com.squareup.moshi.Moshi.Builder]:
 *
 * <pre> `Moshi moshi = new Moshi.Builder()
 * .add(Uri.class, new UriStringJsonAdapter())
 * .build();
`</pre> *
 */
class UriStringJsonAdapter : JsonAdapter<Uri>() {
    @Synchronized
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Uri? {
        return Uri.parse(reader.nextString())
    }

    @Synchronized
    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Uri?) {
        writer.value(value.toString())
    }
}
