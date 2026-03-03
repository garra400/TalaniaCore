package com.talania.core.cosmetics;

import java.util.Objects;

public final class CosmeticVariant {
    private final String model;
    private final String texture;
    private final String icon;
    private final String gradientSet;

    private CosmeticVariant(Builder builder) {
        this.model = builder.model;
        this.texture = builder.texture;
        this.icon = builder.icon;
        this.gradientSet = builder.gradientSet;
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

    public static Builder builder(String model, String texture) {
        return new Builder(model, texture);
    }

    public static final class Builder {
        private final String model;
        private final String texture;
        private String icon = "";
        private String gradientSet = "";

        private Builder(String model, String texture) {
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

        public CosmeticVariant build() {
            return new CosmeticVariant(this);
        }
    }
}
