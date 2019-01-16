package com.joacocampero.liquibaseideaplugin;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.json.JsonElementTypes;
import com.intellij.json.JsonLanguage;
import com.intellij.json.psi.JsonFile;
import com.intellij.json.psi.JsonProperty;
import com.intellij.patterns.PsiElementPattern;
import com.intellij.psi.PsiElement;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class LiquibaseCompletionContributor extends CompletionContributor {

    private static final PsiElementPattern.Capture<PsiElement> ROOT_PROPERTY = psiElement(JsonElementTypes.IDENTIFIER)
            .withSuperParent(4, JsonFile.class)
            .withSuperParent(2, JsonProperty.class)
            .withLanguage(JsonLanguage.INSTANCE);

    private static final PsiElementPattern.Capture<PsiElement> DATABASE_CHANGE_LOG_PROPERTY = psiElement(JsonElementTypes.IDENTIFIER)
            .inside(psiElement().withName("databaseChangeLog"))
            .withSuperParent(2, JsonProperty.class)
//            .andNot(psiElement().withParent(JsonStringLiteral.class))
            .withLanguage(JsonLanguage.INSTANCE);

    public LiquibaseCompletionContributor() {
        extend(CompletionType.BASIC,
                ROOT_PROPERTY,
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    public void addCompletions(@NotNull CompletionParameters completionParameters,
                                               @NotNull ProcessingContext processingContext,
                                               @NotNull CompletionResultSet completionResultSet) {
                        completionResultSet.addElement(LookupElementBuilder.create("databaseChangeLog"));
                    }
                });
        extend(CompletionType.BASIC,
                DATABASE_CHANGE_LOG_PROPERTY,
                new CompletionProvider<CompletionParameters>() {
                    @Override
                    public void addCompletions(@NotNull CompletionParameters completionParameters,
                                               @NotNull ProcessingContext processingContext,
                                               @NotNull CompletionResultSet completionResultSet) {
                        completionResultSet.addElement(LookupElementBuilder.create("changeSet"));
                        completionResultSet.addElement(LookupElementBuilder.create("preConditions"));
                    }
                });
    }

}
