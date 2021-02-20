package gg.jte.compiler.kotlin;

import gg.jte.compiler.ParamInfo;
import gg.jte.compiler.TemplateParserVisitor;

public class KotlinParamInfo {
    public static ParamInfo parse(String parameterString, TemplateParserVisitor visitor) {
        String type;
        String name;
        String defaultValue;
        boolean varargs;

        int nameStartIndex = -1;
        int nameEndIndex = -1;
        int typeStartIndex = -1;
        int typeEndIndex = -1;
        int defaultValueStartIndex = -1;
        int genericDepth = 0;
        int varArgsIndex = parameterString.indexOf("...");
        for (int i = 0; i < parameterString.length(); ++i) {
            char character = parameterString.charAt(i);

            if (character == '<') {
                ++genericDepth;
            } else if (character == '>') {
                --genericDepth;
            }

            if (genericDepth > 0) {
                continue;
            }

            if (nameStartIndex == -1) {
                if (!Character.isWhitespace(character)) {
                    nameStartIndex = i;
                }
            } else if (nameEndIndex == -1) {
                if (Character.isWhitespace(character) || character == ':') {
                    nameEndIndex = i;
                }
            } else if (typeStartIndex == -1) {
                if (!Character.isWhitespace(character)) {
                    typeStartIndex = i;
                }
            } else if (typeEndIndex == -1) {
                if ((Character.isWhitespace(character) && i > varArgsIndex) || character == '=') {
                    typeEndIndex = i;
                } else if (i == parameterString.length() - 1) {
                    typeEndIndex = i + 1;
                    break;
                }
            } else if (defaultValueStartIndex == -1) {
                if (!Character.isWhitespace(character)) {
                    defaultValueStartIndex = i;
                }
            }
        }

        if (typeStartIndex == -1 || typeEndIndex == -1) {
            type = "";
        } else {
            type = parameterString.substring(typeStartIndex, typeEndIndex);
        }

        if (nameEndIndex == -1) {
            nameEndIndex = parameterString.length();
        }

        if (nameStartIndex == -1) {
            visitor.onError("Missing parameter name: '@param " + parameterString + "'");
        }

        name = parameterString.substring(nameStartIndex, nameEndIndex);

        if (defaultValueStartIndex == -1) {
            defaultValue = null;
        } else {
            defaultValue = parameterString.substring(defaultValueStartIndex);
        }

        varargs = varArgsIndex != -1;

        return new ParamInfo(type, name, defaultValue, varargs);
    }
}
