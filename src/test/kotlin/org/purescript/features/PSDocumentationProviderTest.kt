package org.purescript.features

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PSDocumentationProviderTest {
    
    @Test
    fun `test converts doc comments to flowing text`() {
        val documentation =
            PSDocumentationProvider().docCommentsToDocstring(
                listOf(
                    "-- | This is",
                    "-- | main",
                )
            )
        assertEquals("This is\nmain", documentation)
    }

    @Test
    fun `test converts doc comments to paragraphs`() {
        val documentation =
            PSDocumentationProvider().docCommentsToDocstring(
                listOf(
                    "-- | This is",
                    "-- | ",
                    "-- | main",
                )
            )
        assertEquals("This is\n\nmain", documentation)
    }

    @Test
    fun `test converts doc comments with code blocks`() {
        val documentation =
            PSDocumentationProvider().docCommentsToDocstring(
                listOf(
                    "-- | ```purescript",
                    "-- | instance functorF :: Functor F where",
                    "-- |   map = liftM1",
                    "-- | ```",
                )
            )
        assertEquals(
            """<pre><code>
instance functorF :: Functor F where
  map = liftM1
</code></pre>""", documentation)
    }

}