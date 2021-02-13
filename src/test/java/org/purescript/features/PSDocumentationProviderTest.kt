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
        assertEquals("This is main", documentation)
    }
}