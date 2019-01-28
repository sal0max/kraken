package de.salomax.tuck.data

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException
import java.util.Date

/**
 * Formats dates using a Unix Timestamp.
 * To use, add this as an adapter for `Date.class` on your [Moshi.Builder][com.squareup.moshi.Moshi.Builder]:
 *
 * <pre> `Moshi moshi = new Moshi.Builder()
 * .add(Date.class, new UnixTimestampDateJsonAdapter())
 * .build();
`</pre> *
 */
class UnixTimestampDateJsonAdapter : JsonAdapter<Date>() {
    @Synchronized
    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): Date? {
        return Date(reader.nextLong() * 1000)
    }

    @Synchronized
    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: Date?) {
        writer.value(value?.time?.div(1000))
    }
}
