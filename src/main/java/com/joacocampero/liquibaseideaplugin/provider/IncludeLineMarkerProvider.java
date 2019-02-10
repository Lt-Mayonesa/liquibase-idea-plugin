package com.joacocampero.liquibaseideaplugin.provider;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.json.psi.JsonProperty;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class IncludeLineMarkerProvider extends RelatedItemLineMarkerProvider {

    @Override
    protected void collectNavigationMarkers(@NotNull PsiElement element, @NotNull Collection<? super RelatedItemLineMarkerInfo> result) {
        if (element instanceof JsonProperty) {
            PsiLiteralExpression expression = (PsiLiteralExpression) element;
            String value = expression.getValue() instanceof String ? (String) expression.getValue() : null;
            if (value != null) {
                //TODO: follow link
            }
        }
    }
}
