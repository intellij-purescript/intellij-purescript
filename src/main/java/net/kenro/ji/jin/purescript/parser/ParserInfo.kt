package net.kenro.ji.jin.purescript.parser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;

public class ParserInfo {
    public final int position;
    public final LinkedHashSet<Parsec> expected;
    @Nullable
    public final String errorMessage;
    public final boolean success;

    private ParserInfo(final int position, final LinkedHashSet<Parsec> expected, @Nullable final String errorMessage, final boolean success) {
        this.position = position;
        this.success = success;
        this.expected = expected;
        this.errorMessage = errorMessage;
    }

    public ParserInfo(final int position, @Nullable final String errorMessage, final boolean b) {
        this.position = position;
        this.success = b;
        this.expected = new LinkedHashSet<Parsec>();
        this.errorMessage = errorMessage;
    }

    private static <T> LinkedHashSet<T> set(final T obj) {
        final LinkedHashSet<T> result = new LinkedHashSet<T>();
        result.add(obj);
        return result;
    }

    public ParserInfo(final int position, final Parsec expected, final boolean success) {
        this(position, set(expected), null, success);
    }

    public ParserInfo(final int position, final ParserInfo info, final boolean success) {
        this(position, info.expected, null, success);
    }

    public static ParserInfo merge(@NotNull final ParserInfo info1, @NotNull final ParserInfo info2, final boolean success) {
        if (info1.position < info2.position) {
            if (success == info2.success) {
                return info2;
            } else {
                return new ParserInfo(info2.position, info2.expected, info2.errorMessage, success);
            }
        } else if (info1.position > info2.position) {
            if (success == info1.success) {
                return info1;
            } else {
                return new ParserInfo(info1.position, info1.expected, info1.errorMessage, success);
            }
        } else {
            final int position = info1.position;
            final LinkedHashSet<Parsec> expected = new LinkedHashSet<Parsec>();
            expected.addAll(info1.expected);
            expected.addAll(info2.expected);
            return new ParserInfo(position, expected, info1.errorMessage == null ? info2.errorMessage : info1.errorMessage + ";" + info2.errorMessage, success);
        }
    }

    @Override
    public String toString() {
        if (this.errorMessage != null) return this.errorMessage;
        final LinkedHashSet<String> expectedStrings = new LinkedHashSet<String>();
        for (final Parsec parsec : this.expected) {
            expectedStrings.addAll(parsec.getExpectedName());
        }
        final String[] expected = expectedStrings.toArray(new String[expectedStrings.size()]);
        if (expected.length > 0) {
            final StringBuilder sb = new StringBuilder();
            sb.append("Expecting ");
            for (int i = 0; i < expected.length - 2; i++) {
                sb.append(expected[i]).append(", ");
            }
            if (expected.length >= 2) {
                sb.append(expected[expected.length - 2]).append(" or ");
            }

            sb.append(expected[expected.length - 1]);
            return sb.toString();
        }
        return "Error";
    }
}
