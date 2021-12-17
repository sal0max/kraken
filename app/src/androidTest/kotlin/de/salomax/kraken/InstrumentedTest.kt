package de.salomax.kraken

import android.net.Uri
import androidx.core.net.toUri
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import de.salomax.kraken.model.Owner
import de.salomax.kraken.repository.*

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import java.util.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class InstrumentedTest {

    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("de.salomax.kraken.debug", appContext.packageName)
    }

    @Test
    fun parseUri() {
        val uris = listOf(
            Uri.parse("https://www.instagram.com/p/BtKpphFjtRa"),
            Uri.parse("https://www.instagram.com/p/BrnxhiIhsuu?utm_source=ig_share_sheet&igshid=1jd5d0ezsv5ke"),
            Uri.parse("https://www.instagram.com/hildeee/p/BtLzwwjBTNP/?utm_source=ig_sheet/")
        )

        for (uri in uris) {
            assertEquals("www.instagram.com", uri.authority)
            assertNotNull(uri.pathSegments.find { it == "p" })
            assertNotEquals(uri.pathSegments.size - 1, uri.pathSegments.indexOf("p"))
        }

        assertEquals("BtKpphFjtRa", uris[0].pathSegments?.let { it[it.indexOf("p") + 1] } as String)
        assertEquals("BrnxhiIhsuu", uris[1].pathSegments?.let { it[it.indexOf("p") + 1] } as String)
        assertEquals("BtLzwwjBTNP", uris[2].pathSegments?.let { it[it.indexOf("p") + 1] } as String)
    }


    @Test
    fun testInstagramServicePost() {
        val (_, result) = InstagramService.getPost("BpeoORynb0x")
        val post = result.component1()

        assertNotNull(post)
        assertEquals(10, post!!.images.size)
        assertEquals(Owner(728187757, "danielkordan"), post.owner)
        assertEquals(Date("Sun Oct 28 14:32:06 GMT+01:00 2018"), post.dateTime)
        assertEquals(
            Uri.parse(
                "https://instagram.ffra1-1.fna.fbcdn.net/"
                        + "v/"
                        + "t51.2885-15/"
                        + "e35/"
                        + "43403359_170767710533685_8680235362131381289_n.jpg"
                        + "?_nc_ht=instagram.ffra1-1.fna.fbcdn.net"
                        + "&_nc_cat=109"
                        // + "&_nc_ohc=Kd-dDH1SqngAX-rZhd1"
                        // + "&oh=0968c0e7454d5ecb39f34fa1d3a7b921"
                        // + "&oe=5E878B70"
            ),
            post.images[0]
                .imageUrl
                .toString()
                .substringBefore("&_nc_ohc")
                .toUri()
        )
    }


    @Test
    fun testInstagramServiceUser() {
        val (_, result) = InstagramService.getUser("ines.adsm")
        val user = result.component1()

        assertNotNull(user)
        assertEquals(219226146, user!!.id)
        assertEquals(true, user.isPrivate)
        assertEquals(false, user.isVerified)
        assertEquals(false, user.isVerified)
    }

}
