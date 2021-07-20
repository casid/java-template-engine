package gg.jte.runtime;

public final class StringUtils {
    public static boolean startsWithIgnoringCaseAndWhitespaces(String string, String prefix) {
        int j = 0;
        for (int i = 0; i < string.length() && j < prefix.length(); ++i) {
            char c = string.charAt(i);

            if (j == 0 && Character.isWhitespace(c)) {
                continue;
            }

            char p =  prefix.charAt(j++);

            if (p != Character.toLowerCase(c)) {
                return false;
            }
        }

        return true;
    }

    public static boolean isAllUpperCase(String string) {
        for (int i = 0; i < string.length(); ++i) {
            if (!Character.isUpperCase(string.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }

        int strLen = cs.length();
        if (strLen == 0) {
            return true;
        }

        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }

        return true;
    }

    public static void appendEscaped(StringBuilder result, String string) {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (c == '\"') {
                result.append("\\\"");
            } else if (c == '\n') {
                result.append("\\n");
            } else if (c == '\t') {
                result.append("\\t");
            } else if (c == '\r') {
                result.append("\\r");
            } else if (c == '\f') {
                result.append("\\f");
            } else if (c == '\b') {
                result.append("\\b");
            } else if (c == '\\') {
                result.append("\\\\");
            } else {
                result.append(c);
            }
        }
    }

}
