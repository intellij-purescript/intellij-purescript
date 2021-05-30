package org.purescript.ide.actions

import junit.framework.TestCase


class CreateFileActionNameValidatorTest : TestCase() {
    fun `test lowercase is rejected`() {
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose("foo"))
    }

    fun `test uppercase is accepted`() {
        assertTrue(CreateFileAction.NAME_VALIDATOR.canClose("Foo"))
    }

    fun `test empty is rejected`() {
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose(""))
    }

    fun `test blank is rejected`() {
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose("  "))
    }

    fun `test null is rejected`() {
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose(null))
    }

    fun `test qualified is accepted`() {
        assertTrue(CreateFileAction.NAME_VALIDATOR.canClose("Foo.Bar"))
    }

    fun `test weird initial character is rejected`() {
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose(".Foo"))
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose(" Foo"))
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose("*Foo"))
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose("`Foo"))
        assertFalse(CreateFileAction.NAME_VALIDATOR.canClose("3Foo"))
    }
}
