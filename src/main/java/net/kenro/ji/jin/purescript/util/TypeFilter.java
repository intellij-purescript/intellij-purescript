package net.kenro.ji.jin.purescript.util;

import com.intellij.openapi.util.Pair;

import java.util.Set;

public interface TypeFilter {
    boolean testType(String name);

    boolean testTypeMember(String typeName, String memberName);

    String ALL_MEMBERS = "..";

    static TypeFilter always(final boolean value) {
        return new TypeFilter() {
            @Override
            public boolean testType(final String name) {
                return value;
            }

            @Override
            public boolean testTypeMember(final String typeName, final String memberName) {
                return value;
            }
        };
    }

    static TypeFilter fromSets(final Set<String> types, final Set<Pair<String, String>> typeMembers) {
        return new TypeFilter() {
            @Override
            public boolean testType(final String name) {
                return types.contains(name);
            }

            @Override
            public boolean testTypeMember(final String typeName, final String memberName) {
                return typeMembers.contains(Pair.create(typeName, memberName))
                        || typeMembers.contains(Pair.create(typeName, ALL_MEMBERS));
            }
        };
    }

    static TypeFilter byText(final String text) {
        return new TypeFilter() {
            @Override
            public boolean testType(final String name) {
                return text.equals(name);
            }

            @Override
            public boolean testTypeMember(final String typeName, final String memberName) {
                return text.equals(memberName);
            }
        };
    }

    static TypeFilter and(final TypeFilter a, final TypeFilter b) {
        return new TypeFilter() {
            @Override
            public boolean testType(final String name) {
                return a.testType(name) && b.testType((name));
            }

            @Override
            public boolean testTypeMember(final String typeName, final String memberName) {
                return a.testTypeMember(typeName, memberName) && b.testTypeMember(typeName, memberName);
            }
        };
    }
}
