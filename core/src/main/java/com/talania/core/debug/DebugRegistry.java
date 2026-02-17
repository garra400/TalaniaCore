package com.talania.core.debug;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Registry for module-provided debug metadata.
 */
public final class DebugRegistry {
    private final Map<String, DebugModule> modules = new LinkedHashMap<>();

    public DebugModule registerModule(String moduleId, String displayName, Consumer<DebugModuleBuilder> builder) {
        if (moduleId == null || moduleId.isBlank()) {
            throw new IllegalArgumentException("moduleId");
        }
        DebugModule module = new DebugModule(moduleId, displayName == null ? moduleId : displayName);
        DebugModuleBuilder b = new DebugModuleBuilder(module);
        if (builder != null) {
            builder.accept(b);
        }
        modules.put(moduleId, module);
        return module;
    }

    public Collection<DebugModule> modules() {
        return Collections.unmodifiableCollection(modules.values());
    }

    public DebugModule module(String moduleId) {
        return modules.get(moduleId);
    }

    public static final class DebugModuleBuilder {
        private final DebugModule module;

        DebugModuleBuilder(DebugModule module) {
            this.module = module;
        }

        public DebugModuleBuilder section(String id, String title) {
            module.addSection(new DebugModule.DebugSection(id, title));
            return this;
        }
    }
}
