package com.joacocampero.liquibaseideaplugin.provider;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.JsonElementVisitor;
import com.intellij.json.psi.JsonProperty;
import com.intellij.json.psi.impl.JsonRecursiveElementVisitor;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.DocumentAdapter;
import com.joacocampero.liquibaseideaplugin.quickfix.OpenFileQuickFix;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class UsageInspectionProvider extends LocalInspectionTool {

    private final LocalQuickFix fileQuickFix = new OpenFileQuickFix();

    @SuppressWarnings({"WeakerAccess"})
    @NonNls
    public String CHANGELOG_FILE = "changelog-index.json";

    @Nls
    @NotNull
    @Override
    public String getDisplayName() {
        return "Search for databaseChangeLog includes in changelog-index files";
    }


    @Nls
    @NotNull
    @Override
    public String getGroupDisplayName() {
        return "Liquibase";
    }

    @NotNull
    @Override
    public String getShortName() {
        return "UsageInspectionProvider";
    }

    @NotNull
    @Override
    public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
        return new JsonElementVisitor() {
            @Override
            public void visitFile(PsiFile file) {
                super.visitFile(file);
                String path = file.getVirtualFile().getPath();
                if (!file.getName().equals(CHANGELOG_FILE)) {
                    PsiFile[] psiFiles = FilenameIndex.getFilesByName(file.getProject(), CHANGELOG_FILE, GlobalSearchScope.everythingScope(file.getProject()));
                    if (!isFileInChangeLog(psiFiles, path)) {
                        holder.registerProblem(file, "This changelog is not included in any changelog-index file.", fileQuickFix);
                    }
                }
            }
        };
    }

    private boolean isFileInChangeLog(PsiFile[] psiFiles, String filePath) {
        List<String> includes = new ArrayList<>();
        for (PsiFile file : psiFiles) {
            file.acceptChildren(new JsonRecursiveElementVisitor() {
                @Override
                public void visitProperty(@NotNull JsonProperty o) {
                    super.visitProperty(o);
                    if (o.getName().equals("file")) {
                        if (o.getValue() != null) {
                            includes.add(o.getValue().getText().replace("\"", ""));
                        }
                    }
                }
            });
        }
        return includes.contains(filePath);
    }

    @Nullable
    @Override
    public JComponent createOptionsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        final JTextField checkedClasses = new JTextField(CHANGELOG_FILE);
        checkedClasses.getDocument().addDocumentListener(new DocumentAdapter() {
            public void textChanged(@NotNull DocumentEvent event) {
                CHANGELOG_FILE = checkedClasses.getText();
            }
        });

        panel.add(checkedClasses);
        return panel;
    }
}
