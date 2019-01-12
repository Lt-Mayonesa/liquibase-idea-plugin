package com.joacocampero.liquibaseideaplugin;

import com.intellij.json.psi.JsonArray;
import com.intellij.json.psi.JsonProperty;
import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.Annotator;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;

public class LiquibaseJsonAnnotator implements Annotator {
    @Override
    public void annotate(@NotNull PsiElement element, @NotNull AnnotationHolder holder) {
        if (element instanceof JsonProperty) {
            if (((JsonProperty) element).getName().equals("databaseChangeLog")) {
                if (!(element.getLastChild() instanceof JsonArray)) {
                    TextRange range = new TextRange(
                            element.getTextRange().getStartOffset(), element.getTextRange().getEndOffset());
                    holder.createErrorAnnotation(range, "databaseChangeLog must be a Json Array");
                }
                PsiFile[] psiFiles = FilenameIndex.getFilesByName(element.getProject(), "changelog-index.json", GlobalSearchScope.everythingScope(element.getProject()));
                for (PsiFile file : psiFiles) {
                    Document document = PsiDocumentManager.getInstance(element.getProject()).getDocument(file);
                    if (document != null) {
                        String text = document.getText();
                    }
                }
            }
        }
    }
}
