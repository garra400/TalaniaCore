package com.talania.core.cosmetics;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class CosmeticDefinition {
    private final String id;
    private final String slot;
    private final String model;
    private final String texture;
    private final String icon;
    private final String gradientSet;
    private final Map<String, CosmeticVariant> variants;
    private final List<String> slotOverrides;

    private CosmeticDefinition(Builder builder) {
        this.id = builder.id;
        this.slot = builder.slot;
        this.model = builder.model;
        this.texture = builder.texture;
        this.icon = builder.icon;
        this.gradientSet = builder.gradientSet;
        this.variants = builder.variants != null ? builder.variants : Collections.emptyMap();
        this.slotOverrides = builder.slotOverrides != null ? builder.slotOverrides : List.of();
    }

    public String id() {
        return id;
    }

    public String slot() {
        return slot;
    }

    public String model() {
        return model;
    }

    public String texture() {
        return texture;
    }

    public String icon() {
        return icon;
    }

    public String gradientSet() {
        return gradientSet;
    }

    public Map<String, CosmeticVariant> variants() {
        return variants;
    }

    public List<String> slotOverrides() {
        return slotOverrides;
    }

    public static Builder builder(String id, String slot, String model, String texture) {
        return new Builder(id, slot, model, texture);
    }

    public static final class Builder {
        private final String id;
        private final String slot;
        private final String model;
        private final String texture;
        private String icon = "";
        private String gradientSet = "";
        private Map<String, CosmeticVariant> variants;
        private List<String> slotOverrides;

        private Builder(String id, String slot, String model, String texture) {
            this.id = Objects.requireNonNull(id, "id");
            this.slot = Objects.requireNonNull(slot, "slot");
            this.model = Objects.requireNonNull(model, "model");
            this.texture = Objects.requireNonNull(texture, "texture");
        }

        public Builder icon(String icon) {
            this.icon = icon != null ? icon : "";
            return this;
        }

        public Builder gradientSet(String gradientSet) {
            this.gradientSet = gradientSet != null ? gradientSet : "";
            return this;
        }

        public Builder variants(Map<String, CosmeticVariant> variants) {
            this.variants = variants;
            return this;
        }

        public Builder slotOverrides(List<String> slotOverrides) {
            this.slotOverrides = slotOverrides;
            return this;
        }

        public CosmeticDefinition build() {
            return new CosmeticDefinition(this);
        }
    }
}
