package net.kenro.ji.jin.purescript.psi.cst;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiLanguageInjectionHost;
import com.intellij.psi.impl.source.tree.LeafElement;
import com.intellij.psi.tree.IElementType;
import net.kenro.ji.jin.purescript.psi.PSElements;
import net.kenro.ji.jin.purescript.psi.PSTokens;
import net.kenro.ji.jin.purescript.psi.impl.PSProperNameImpl;
import org.jetbrains.annotations.NotNull;

public class PSASTWrapperElement extends ASTWrapperPsiElement implements PsiLanguageInjectionHost {
    public PSASTWrapperElement(final ASTNode astNode) {
        super(astNode);
    }

    public boolean isString() {
        final IElementType type = getNode().getElementType();
        return type.equals(PSElements.JSRaw) || type.equals(PSElements.StringLiteral);
    }

    public boolean isBlockString() {
        return getStringText().startsWith("\"\"\"");
    }

    public boolean isMultilineString() {
        return getStringText().indexOf('\n') != -1;
    }

    /**
     * Returns the text of a string element, including its quotes.
     */
    @NotNull
    public String getStringText() {
        return isString() ? getNode().getFirstChildNode().getText() : "";
    }

    @Override
    public boolean isValidHost() {
        // Only supports block-strings or single-line strings.
        return isString() && (isBlockString() || !isMultilineString());
    }

    @Override
    public PSASTWrapperElement updateText(@NotNull final String s) {
        final ASTNode valueNode = getNode().getFirstChildNode();
        assert valueNode instanceof LeafElement;
        ((LeafElement)valueNode).replaceWithText(s);
        return this;
    }

    @NotNull
    @Override
    public PSStringLiteralEscaper createLiteralTextEscaper() {
        return new PSStringLiteralEscaper(this);
    }
}
