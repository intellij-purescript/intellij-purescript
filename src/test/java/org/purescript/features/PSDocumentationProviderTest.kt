package org.purescript.features

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import junit.framework.TestCase

class PSDocumentationProviderTest : TestCase() {
    fun `test converts doc comments to flowing text`() {
        val documentation =
            PSDocumentationProvider().docCommentsToDocstring(
                listOf(
                    "-- | This is",
                    "-- | main",
                )
            )
        assertEquals("<p>This is\nmain</p>", documentation)
    }

    fun `test converts doc comments to paragraphs`() {
        val documentation =
            PSDocumentationProvider().docCommentsToDocstring(
                listOf(
                    "-- | This is",
                    "-- | ",
                    "-- | main",
                )
            )
        assertEquals("<p>This is</p>\n\n<p>main</p>", documentation)
    }
}