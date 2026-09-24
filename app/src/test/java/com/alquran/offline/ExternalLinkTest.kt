package com.alquran.offline

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.net.URI

class ExternalLinkTest {

    @Test
    fun testWhatsAppChannelLink() {
        val waChannel = "https://whatsapp.com/channel/0029VbDIlIVFnSzEiKLZ6P0h"

        val uri = URI(waChannel)
        assertEquals("https", uri.scheme)
        assertEquals("whatsapp.com", uri.host)
        assertTrue(uri.path.contains("0029VbDIlIVFnSzEiKLZ6P0h"))
    }

    @Test
    fun testTelegramLink() {
        val tg = "https://t.me/SuikaKings"

        val uri = URI(tg)
        assertEquals("https", uri.scheme)
        assertEquals("t.me", uri.host)
        assertEquals("/SuikaKings", uri.path)
    }

    @Test
    fun testOpenUrlSafelyProtection() {
        // Verify valid URLs pass syntax check without throwing exceptions
        val urls = listOf(
            "https://whatsapp.com/channel/0029VbDIlIVFnSzEiKLZ6P0h",
            "https://t.me/SuikaKings"
        )

        for (url in urls) {
            val uri = URI.create(url)
            assertTrue("Scheme must be https", uri.scheme == "https")
            assertTrue("Host must not be empty", !uri.host.isNullOrEmpty())
        }
    }
}
