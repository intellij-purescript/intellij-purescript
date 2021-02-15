package org.purescript.features

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