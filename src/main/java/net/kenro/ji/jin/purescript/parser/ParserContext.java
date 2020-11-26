package net.kenro.ji.jin.purescript.parser;

import java.util.HashMap;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.WhitespacesAndCommentsBinder;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.containers.Stack;
import net.kenro.ji.jin.purescript.psi.PSTokens;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ParserContext {
    @NotNull
    private final PsiBuilder builder;
    private int column;
    private final Stack<Integer> indentationLevel = new Stack<Integer>();
    private final HashMap<IElementType, Integer> recoverySet = new HashMap<IElementType, Integer>();
    private boolean inAttempt;
    private int inOptional;

    public boolean eof() {
        return builder.eof();
    }

    private final class PureMarker implements PsiBuilder.Marker {
        private final int start;
        private final PsiBuilder.Marker marker;

        protected PureMarker(@NotNull final PsiBuilder.Marker marker) {
            this.start = column;
            this.marker = marker;
        }

        public PureMarker(final int start, final PsiBuilder.Marker marker) {
            this.start = start;
            this.marker = marker;
        }

        @Override
        public PsiBuilder.Marker precede() {
            return new PureMarker(start, marker);
        }

        @Override
        public void drop() {
            marker.drop();
        }

        @Override
        public void rollbackTo() {
            column = start;
            marker.rollbackTo();
        }

        @Override
        public void done(final IElementType type) {
            marker.done(type);
        }

        @Override
        public void collapse(final IElementType type) {
            marker.collapse(type);
        }

        @Override
        public void doneBefore(final IElementType type, final PsiBuilder.Marker before) {
            marker.doneBefore(type, before);
        }

        @Override
        public void doneBefore(final IElementType type, final PsiBuilder.Marker before, final String errorMessage) {
            marker.doneBefore(type, before, errorMessage);
        }

        @Override
        public void error(final String message) {
            marker.error(message);
        }

        @Override
        public void errorBefore(final String message, final PsiBuilder.Marker before) {
            marker.errorBefore(message, before);
        }

        @Override
        public void setCustomEdgeTokenBinders(@Nullable final WhitespacesAndCommentsBinder left, @Nullable final WhitespacesAndCommentsBinder right) {
            marker.setCustomEdgeTokenBinders(left, right);
        }
    }

    public ParserContext(@NotNull final PsiBuilder builder) {
        this.builder = builder;
        this.indentationLevel.push(0);
    }

    public void whiteSpace() {
        while (!builder.eof()) {
            final IElementType type = builder.getTokenType();
            if (type == PSTokens.WS) {
                advance();
            } else {
                break;
            }
        }
    }

    public void advance() {
        final String text = builder.getTokenText();
        if (text != null) {
            final IElementType type = builder.getTokenType();
            if (type == PSTokens.STRING || type == PSTokens.WS) {
                for (int i = 0; i < text.length(); i++) {
                    final char ch = text.charAt(i);
                    if (ch == '\n') {
                        column = 0;
                    } else if (ch == '\t') {
                        column = column - column % 8 + 8;
                    } else {
                        column++;
                    }
                }
            } else {
                column += text.length();
            }
        }
        builder.advanceLexer();
    }

    public void addUntilToken(@NotNull final IElementType token) {
        int i = 0;
        if (recoverySet.containsKey(token)) {
            i = recoverySet.get(token);
        }
        recoverySet.put(token, i + 1);
    }

    public void removeUntilToken(@NotNull final IElementType token) {
        final int i = recoverySet.get(token);
        if (i == 1) {
            recoverySet.remove(token);
        } else {
            recoverySet.put(token, i - 1);
        }
    }

    public boolean isUntilToken(@NotNull final IElementType token) {
        return recoverySet.containsKey(token);
    }

    public void setInAttempt(final boolean inAttempt) {
        this.inAttempt = inAttempt;
    }


    public boolean isInAttempt() {
        return this.inAttempt;
    }

    public void enterOptional() {
        this.inOptional++;
    }

    public void exitOptional() {
        this.inOptional--;
    }

    public boolean isInOptional() {
        return inOptional > 0;
    }

    @NotNull
    public String text() {
        final String text = builder.getTokenText();
        if (text == null) return "";
        return text;
    }

    @NotNull
    public IElementType peek() {
        final IElementType tokenType = builder.getTokenType();
        return tokenType == null ? PSTokens.EOF : tokenType;
    }

    public boolean match(@NotNull final IElementType type) {
        return builder.getTokenType() == type;
    }

    public boolean eat(@NotNull final IElementType type) {
        if (builder.getTokenType() == type) {
            advance();
            return true;
        }
        return false;
    }

    public boolean expect(@NotNull final IElementType type) {
        final PsiBuilder.Marker mark = builder.mark();
        if (builder.getTokenType() == type) {
            advance();
            mark.drop();
            return true;
        }
        mark.error(String.format("Expecting %s.", type.toString()));
        return false;
    }

    @NotNull
    public PsiBuilder.Marker start() {
        // Consume all the white spaces.
        builder.eof();
        return new PureMarker(builder.mark());
    }

    public int getPosition() {
        return builder.getCurrentOffset();
    }

    public int getColumn() {
        return column;
    }

    public int getIndentationLevel() {
        return indentationLevel.peek();
    }

    public int getLastIndentationLevel() {
        if (indentationLevel.size() >= 2) {
            return indentationLevel.get(indentationLevel.size() - 2);
        }
        return 0;
    }

    public void pushIndentationLevel() {
        indentationLevel.push(column);
    }

    public void popIndentationLevel() {
        indentationLevel.tryPop();
    }

    @NotNull
    public String getText(final int start, final int end) {
        return this.builder.getOriginalText().subSequence(start, end).toString();
    }
}
