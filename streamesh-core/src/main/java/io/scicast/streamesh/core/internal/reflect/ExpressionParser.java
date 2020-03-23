package io.scicast.streamesh.core.internal.reflect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExpressionParser {


    public static boolean isExpression(String value) {
        return value.trim().startsWith("${") && value.trim().endsWith("}");
    }

    public static List<String> parse(String value) {
        value = value.trim();
        if (!isExpression(value)) {
            return new ArrayList<>();
        }

        value = value.replaceAll("\\$\\{", "");
        value = value.replaceAll("}", "");
        return Arrays.asList(value.split("\\."));
    }


}
