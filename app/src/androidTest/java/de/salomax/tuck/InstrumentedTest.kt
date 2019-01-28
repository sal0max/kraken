package de.salomax.tuck

import android.net.Uri
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import de.salomax.tuck.data.InstagramService
import de.salomax.tuck.data.Owner

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
        val appContext = InstrumentationRegistry.getTargetContext()
        assertEquals("de.salomax.tuck", appContext.packageName)
    }

    @Test
    fun parseUri() {
        val s1 = Uri.parse("https://www.instagram.com/p/BtKpphFjtRa")
        val s2 = Uri.parse("https://www.instagram.com/p/BrncKOOB83d?utm_source=ig_share_sheet")
        val s3 = Uri.parse("https://www.instagram.com/p/BrnxhiIhsuu?utm_source=ig_share_sheet&igshid=1jd5d0ezsv5ke")

        assertEquals(2, s1.pathSegments.size)
        assertEquals(2, s2.pathSegments.size)
        assertEquals(2, s3.pathSegments.size)

        assertEquals("p", s1.pathSegments[0])
        assertEquals("p", s2.pathSegments[0])
        assertEquals("p", s3.pathSegments[0])

        assertEquals("BtKpphFjtRa", s1.pathSegments[1])
        assertEquals("BrncKOOB83d", s2.pathSegments[1])
        assertEquals("BrnxhiIhsuu", s3.pathSegments[1])
    }


    @Test
    fun testInstagramService() {
        // https://www.instagram.com/p/BpeoORynb0x/
        val post = InstagramService.create().getPost("BpeoORynb0x").blockingFirst()

        assertEquals(10, post.images.size)
        assertEquals(Owner(728187757, "danielkordan"), post.owner)
        assertEquals(Date("Sun Oct 28 14:32:06 GMT+01:00 2018"), post.dateTime)
        assertEquals(
            Uri.parse(
                "https://scontent-frx5-1.cdninstagram.com/" +
                        "vp/" +
                        "a13401a64fa0b0f833cffea732880b2f/" +
                        "5CEEDA70/" +
                        "t51.2885-15/" +
                        "e35/" +
                        "43403359_170767710533685_8680235362131381289_n.jpg" +
                        "?_nc_ht=scontent-frx5-1.cdninstagram.com"
            ),
            post.images[0].imageUrl
        )
    }

}
