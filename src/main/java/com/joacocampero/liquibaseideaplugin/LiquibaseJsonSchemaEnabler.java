package com.joacocampero.liquibaseideaplugin;

import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.jsonSchema.extension.JsonSchemaEnabler;

public class LiquibaseJsonSchemaEnabler implements JsonSchemaEnabler {
    @Override
    public boolean isEnabledForFile(VirtualFile file) {
        return file.getFileType().getDefaultExtension().equals(".json");
    }
}
