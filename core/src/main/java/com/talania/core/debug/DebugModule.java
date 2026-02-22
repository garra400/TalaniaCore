package com.talania.core.debug;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Debug registration info for a module.
 */
public final class DebugModule {
    private final String moduleId;
    private final String displayName;
    private final List<DebugSection> sections = new ArrayList<>();

    DebugModule(String moduleId, String displayName) {
        this.moduleId = moduleId;
        this.displayName = displayName;
    }

    public String moduleId() {
        return moduleId;
    }

    public String displayName() {
        return displayName;
    }

    public List<DebugSection> sections() {
        return Collections.unmodifiableList(sections);
    }

    void addSection(DebugSection section) {
        if (section != null) {
            sections.add(section);
        }
    }

    public static final class DebugSection {
        private final String id;
        private final String title;

        public DebugSection(String id, String title) {
            this.id = id;
            this.title = title;
        }

        public String id() {
            return id;
        }

        public String title() {
            return title;
        }
    }
}
