package net.kenro.ji.jin.purescript.psi.cst;

import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.AbstractElementManipulator;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

public class PSStringManipulator extends AbstractElementManipulator<PSASTWrapperElement> {
    @Override
    public PSASTWrapperElement handleContentChange(@NotNull final PSASTWrapperElement psi, @NotNull final TextRange range, final String newContent) throws IncorrectOperationException {
        final String oldText = psi.getText();
        final String newText = oldText.substring(0, range.getStartOffset()) + newContent + oldText.substring(range.getEndOffset());
        return psi.updateText(newText);
    }

    @NotNull
    @Override
    public TextRange getRangeInElement(@NotNull final PSASTWrapperElement element) {
        return pairToTextRange(element.isBlockString() ? getRangeForBlockString(element) : getRangeForString(element));
    }

    private static Pair<Integer, Integer> getRangeForBlockString(@NotNull final PSASTWrapperElement element) {
        final String text = element.getStringText();
        final int start = text.indexOf("\"\"\"") + 3;
        final int end = text.lastIndexOf("\"\"\"") - start;
        return new Pair<Integer, Integer>(start, end);
    }

    private static Pair<Integer, Integer> getRangeForString(@NotNull final PSASTWrapperElement element) {
        final String text = element.getStringText();
        final int start = text.indexOf("\"") + 1;
        final int end = text.lastIndexOf("\"") - start;
        return new Pair<Integer, Integer>(start, end);
    }

    private static TextRange pairToTextRange(final Pair<Integer, Integer> pair) {
        final int start = Math.max(pair.first, 0);
        final int end = Math.max(pair.second, start);
        return TextRange.from(start, end);
    }
}
