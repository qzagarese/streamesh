package io.scicast.streamesh.core.internal.reflect.handler;

import io.scicast.streamesh.core.StreameshContext;
import io.scicast.streamesh.core.internal.reflect.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ResolvableHandler implements GrammarMarkerHandler<Resolvable> {

    @Override
    public HandlerResult handle(ScopeContext scopeContext, StreameshContext context) {
        Resolvable annotation = (Resolvable) scopeContext.getAnnotation();

        if (!(scopeContext.getInstance() instanceof String)) {
            throw new IllegalArgumentException(
                    String.format("@%s can only be used on fields of type String.", Resolvable.class.getName()));
        }

        List<String> scopePath = Arrays.asList(annotation.scope().trim().split("\\.")).stream().collect(Collectors.toList());
        if (scopePath.size() == 0) {
            throw new IllegalArgumentException(String.format("Scope in @%s cannot be empty.", Resolvable.class.getName()));
        }
        List<String> basePath = buildBasePath(scopeContext, scopePath);
        String resolvableValue = (String) scopeContext.getInstance();

        Scope resultScope = scopeContext.getScope();
        if (ExpressionParser.isExpression(resolvableValue)) {
            List<String> expressionPath = ExpressionParser.parse(resolvableValue);
            String attribute = ((Field) scopeContext.getTarget()).getName();
            ValueDependency dependency = ValueDependency.builder()
                    .attribute(attribute)
                    .path(Stream.concat(basePath.stream(), expressionPath.stream()).collect(Collectors.toList()))
                    .build();

            Scope subsScope = scopeContext.getScope().subScope(scopeContext.getParentPath());

            subsScope = subsScope.withDependencies(
                    Stream.concat(
                            subsScope.getDependencies().stream(),
                            Stream.of(dependency))
                            .collect(Collectors.toList()));
            resultScope = resultScope.attach(subsScope, scopeContext.getParentPath(), true);
        }


        return HandlerResult.builder()
                .resultScope(resultScope)
                .targetMountPoint(scopeContext.getParentPath())
                .targetValue(scopeContext.getInstance())
                .build();
    }

    private List<String> buildBasePath(ScopeContext scopeContext, List<String> scopePath) {
        List<String> basePath;
        if (scopePath.get(0).equals("root")) {
            basePath = new ArrayList<>();
            scopePath.remove(0);
        } else if (scopePath.get(0).equals("parent")) {
            List<String> cleanedScopePath = scopePath.stream()
                    .filter(s -> !s.equals("parent"))
                    .collect(Collectors.toList());

            int offset = scopePath.size() - cleanedScopePath.size();
            basePath = scopeContext.getParentPath().stream().collect(Collectors.toList());

            while (offset > 0) {
                if (basePath.size() - offset > 0) {
                    basePath.remove(basePath.size() - offset);
                }
                offset--;
            }
            scopePath = cleanedScopePath;
        } else {
            basePath = scopeContext.getParentPath();
        }
        return Stream.concat(basePath.stream(), scopePath.stream()).collect(Collectors.toList());
    }
}
