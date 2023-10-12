package io.smallrye.openapi.runtime.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.jboss.jandex.ArrayType;
import org.jboss.jandex.ClassType;
import org.jboss.jandex.DotName;
import org.jboss.jandex.ParameterizedType;
import org.jboss.jandex.Type;
import org.jboss.jandex.WildcardType;

/**
 * Parse a type signature String to a Jandex Type
 */
public class TypeParser {

    private static final WildcardType UNBOUNDED_WILDCARD = WildcardType.createUpperBound((Type) null);
    private String signature;
    private int pos;

    public static Type parse(String signature) {
        return new TypeParser(signature).parse();
    }

    private TypeParser(String signature) {
        this.signature = signature;
        this.pos = 0;
    }

    private Type parse() {
        return parseReferenceType();
    }

    private Type parseClassTypeSignature() {
        DotName name = parseName();
        Type[] types = parseTypeArguments();
        Type type = null;

        if (types.length > 0) {
            type = ParameterizedType.create(name, types, null);
        }

        return type != null ? type : ClassType.create(name);
    }

    private Type[] parseTypeArguments() {
        if (pos >= signature.length() || signature.charAt(pos) != '<') {
            return Type.EMPTY_ARRAY;
        }
        pos++;

        List<Type> types = new ArrayList<>();
        for (;;) {
            Type t = parseTypeArgument();
            if (t == null) {
                break;
            }
            advanceNot(',');
            types.add(t);
        }
        return types.toArray(new Type[types.size()]);
    }

    private Type parseTypeArgument() {
        requireIncomplete();
        char c = signature.charAt(pos++);

        if (c == '>') {
            return null;
        }
        if (c == '?') {
            if (signature.startsWith(" extends ", pos)) {
                pos += " extends ".length();
                return parseWildCard(true);
            } else if (signature.startsWith(" super ", pos)) {
                pos += " super ".length();
                return parseWildCard(false);
            } else {
                requireIncomplete();
                if (signature.charAt(pos) != '>') {
                    throw new IllegalStateException();
                }
                return UNBOUNDED_WILDCARD;
            }
        }

        pos--;
        return parseReferenceType();
    }

    private Type parseWildCard(boolean isExtends) {
        Type bound = parseReferenceType();

        return isExtends ? WildcardType.createUpperBound(bound) : WildcardType.createLowerBound(bound);
    }

    private Type parseReferenceType() {
        int mark = pos;
        int typeArgsStart = signature.indexOf('<', mark);
        int typeArgsEnd = signature.indexOf('>', mark);
        int arrayStart = signature.indexOf('[', mark);

        return Stream.of(typeArgsEnd, typeArgsStart, arrayStart)
                .filter(v -> v > -1)
                .min(Integer::compare)
                .map(firstDelimiter -> {
                    Type type = null;

                    if (firstDelimiter == arrayStart) {
                        type = parsePrimitive();
                    }

                    if (type == null) {
                        type = parseClassTypeSignature();
                    }

                    if (pos < signature.length() && signature.charAt(pos) == '[') {
                        type = parseArrayType(type);
                    }

                    return type;
                })
                .orElseGet(() -> {
                    Type primitive = parsePrimitive();

                    if (primitive != null) {
                        return primitive;
                    }

                    return parseClassTypeSignature();
                });
    }

    private Type parseArrayType(Type type) {
        int dimensions = 0;

        while (pos < signature.length() && signature.charAt(pos) == '[') {
            pos++;
            requireIncomplete();

            if (signature.charAt(pos++) == ']') {
                dimensions++;
            } else {
                throw new IllegalArgumentException();
            }
        }

        return ArrayType.create(type, dimensions);
    }

    private Type parsePrimitive() {
        int mark = pos;
        DotName name = parseName();
        Type type = Type.create(name, Type.Kind.PRIMITIVE);
        if (type != null) {
            return type;
        }
        pos = mark;
        return null;
    }

    private int advanceNot(char c) {
        requireIncomplete();

        while (signature.charAt(pos) == c) {
            pos++;
        }

        return pos;
    }

    private DotName parseName() {
        int start = pos;
        int end = advanceNameEnd();
        return DotName.createSimple(signature.substring(start, end));
    }

    private int advanceNameEnd() {
        int end = pos;

        for (; end < signature.length(); end++) {
            char c = signature.charAt(end);
            if (c == '[' || c == '<' || c == ',' || c == '>') {
                return pos = end;
            }
        }

        return pos = end;
    }

    private void requireIncomplete() {
        if (pos >= signature.length()) {
            throw new IllegalStateException("Unexpected end of input");
        }
    }
}
