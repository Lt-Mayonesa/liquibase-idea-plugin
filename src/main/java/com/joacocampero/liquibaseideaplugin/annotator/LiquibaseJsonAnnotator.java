package com.joacocampero.liquibaseideaplugin.annotator;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class LiquibaseJsonAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (!element.getContainingFile().getName().equals("changelog-index.json")) {
            if (element instanceof JsonProperty) {
                if (((JsonProperty) element).getName().equals("databaseChangeLog")) {
                    if (!(element.getLastChild() instanceof JsonArray)) {
                        TextRange range = new TextRange(
                                element.getTextRange().getStartOffset(), element.getTextRange().getEndOffset());
                        holder.createErrorAnnotation(range, "databaseChangeLog must be a Json Array");
                    }
                }
            }
        }
    }
}
