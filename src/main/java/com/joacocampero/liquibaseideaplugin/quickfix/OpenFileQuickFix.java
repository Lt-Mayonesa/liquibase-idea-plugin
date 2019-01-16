package com.joacocampero.liquibaseideaplugin.quickfix;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.util.TreeFileChooser;
import com.intellij.ide.util.TreeFileChooserFactory;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class OpenFileQuickFix implements LocalQuickFix {

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getFamilyName() {
        return "OpenFile";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        TreeFileChooser fileChooser = TreeFileChooserFactory
                .getInstance(project)
                .createFileChooser("Pick changelog-index file",
                        null,
                        JsonFileType.INSTANCE,
                        file -> file.getName().contains("changelog-index"));
        fileChooser.showDialog();
        if (fileChooser.getSelectedFile() != null) {
            OpenFileDescriptor openFileDescriptor = new OpenFileDescriptor(project, fileChooser.getSelectedFile().getVirtualFile(), 0);
            openFileDescriptor.navigate(true);
        }
    }
}
