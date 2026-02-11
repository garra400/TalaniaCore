package com.talania.core.ui;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Factory for creating UI components in a library-agnostic way.
 * 
 * <p>Provides a fluent API for building UI elements that can be
 * adapted to different UI backends (Simple UI, HyUI, etc.).
 * 
 * <p>Usage:
 * <pre>{@code
 * // Set the adapter during initialization
 * UIFactory.setAdapter(new SimpleUIAdapter());
 * 
 * // Create components using the fluent API
 * UIComponent button = UIFactory.button()
 *     .text("Click Me")
 *     .position(100, 50)
 *     .size(120, 40)
 *     .onClick(() -> System.out.println("Clicked!"))
 *     .build();
 * 
 * // Add to screen
 * screen.add(button.getNative());
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class UIFactory {

    private static UIAdapter adapter;

    private UIFactory() {}

    // ==================== CONFIGURATION ====================

    /**
     * Set the UI adapter for the current platform.
     * Must be called during mod initialization.
     * 
     * @param uiAdapter The adapter implementation
     */
    public static void setAdapter(UIAdapter uiAdapter) {
        adapter = uiAdapter;
    }

    /**
     * Get the current adapter.
     */
    public static UIAdapter getAdapter() {
        return adapter;
    }

    private static void ensureAdapter() {
        if (adapter == null) {
            throw new IllegalStateException("UIFactory adapter not set. Call UIFactory.setAdapter() first.");
        }
    }

    // ==================== COMPONENT BUILDERS ====================

    /**
     * Create a button builder.
     */
    public static ButtonBuilder button() {
        ensureAdapter();
        return new ButtonBuilder();
    }

    /**
     * Create a label builder.
     */
    public static LabelBuilder label() {
        ensureAdapter();
        return new LabelBuilder();
    }

    /**
     * Create a panel/container builder.
     */
    public static PanelBuilder panel() {
        ensureAdapter();
        return new PanelBuilder();
    }

    /**
     * Create an image builder.
     */
    public static ImageBuilder image() {
        ensureAdapter();
        return new ImageBuilder();
    }

    // ==================== ADAPTER INTERFACE ====================

    /**
     * Adapter interface for UI library integration.
     * Implement this for your target UI library.
     */
    public interface UIAdapter {
        Object createButton(ButtonBuilder builder);
        Object createLabel(LabelBuilder builder);
        Object createPanel(PanelBuilder builder);
        Object createImage(ImageBuilder builder);
    }

    /**
     * Generic wrapper for native UI components.
     */
    public static class UIComponent {
        private final Object nativeComponent;
        private final String type;

        public UIComponent(Object nativeComponent, String type) {
            this.nativeComponent = nativeComponent;
            this.type = type;
        }

        @SuppressWarnings("unchecked")
        public <T> T getNative() {
            return (T) nativeComponent;
        }

        public String getType() {
            return type;
        }
    }

    // ==================== BASE BUILDER ====================

    public abstract static class BaseBuilder<T extends BaseBuilder<T>> {
        protected float x, y;
        protected float width, height;
        protected String id;
        protected boolean visible = true;
        protected Color backgroundColor;

        @SuppressWarnings("unchecked")
        protected T self() {
            return (T) this;
        }

        public T id(String id) {
            this.id = id;
            return self();
        }

        public T position(float x, float y) {
            this.x = x;
            this.y = y;
            return self();
        }

        public T size(float width, float height) {
            this.width = width;
            this.height = height;
            return self();
        }

        public T visible(boolean visible) {
            this.visible = visible;
            return self();
        }

        public T backgroundColor(Color color) {
            this.backgroundColor = color;
            return self();
        }

        public float getX() { return x; }
        public float getY() { return y; }
        public float getWidth() { return width; }
        public float getHeight() { return height; }
        public String getId() { return id; }
        public boolean isVisible() { return visible; }
        public Color getBackgroundColor() { return backgroundColor; }
    }

    // ==================== BUTTON BUILDER ====================

    public static class ButtonBuilder extends BaseBuilder<ButtonBuilder> {
        private String text;
        private Runnable onClick;
        private Supplier<String> textSupplier;
        private boolean enabled = true;
        private Color textColor = Color.WHITE;

        public ButtonBuilder text(String text) {
            this.text = text;
            return this;
        }

        public ButtonBuilder dynamicText(Supplier<String> supplier) {
            this.textSupplier = supplier;
            return this;
        }

        public ButtonBuilder onClick(Runnable handler) {
            this.onClick = handler;
            return this;
        }

        public ButtonBuilder enabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public ButtonBuilder textColor(Color color) {
            this.textColor = color;
            return this;
        }

        public UIComponent build() {
            Object native_ = adapter.createButton(this);
            return new UIComponent(native_, "button");
        }

        public String getText() { return textSupplier != null ? textSupplier.get() : text; }
        public Runnable getOnClick() { return onClick; }
        public boolean isEnabled() { return enabled; }
        public Color getTextColor() { return textColor; }
    }

    // ==================== LABEL BUILDER ====================

    public static class LabelBuilder extends BaseBuilder<LabelBuilder> {
        private String text;
        private Supplier<String> textSupplier;
        private Color textColor = Color.WHITE;
        private float fontSize = 14;
        private TextAlign align = TextAlign.LEFT;

        public LabelBuilder text(String text) {
            this.text = text;
            return this;
        }

        public LabelBuilder dynamicText(Supplier<String> supplier) {
            this.textSupplier = supplier;
            return this;
        }

        public LabelBuilder textColor(Color color) {
            this.textColor = color;
            return this;
        }

        public LabelBuilder fontSize(float size) {
            this.fontSize = size;
            return this;
        }

        public LabelBuilder align(TextAlign align) {
            this.align = align;
            return this;
        }

        public UIComponent build() {
            Object native_ = adapter.createLabel(this);
            return new UIComponent(native_, "label");
        }

        public String getText() { return textSupplier != null ? textSupplier.get() : text; }
        public Color getTextColor() { return textColor; }
        public float getFontSize() { return fontSize; }
        public TextAlign getAlign() { return align; }
    }

    // ==================== PANEL BUILDER ====================

    public static class PanelBuilder extends BaseBuilder<PanelBuilder> {
        private Layout layout = Layout.NONE;
        private float padding = 0;
        private float spacing = 0;

        public PanelBuilder layout(Layout layout) {
            this.layout = layout;
            return this;
        }

        public PanelBuilder padding(float padding) {
            this.padding = padding;
            return this;
        }

        public PanelBuilder spacing(float spacing) {
            this.spacing = spacing;
            return this;
        }

        public UIComponent build() {
            Object native_ = adapter.createPanel(this);
            return new UIComponent(native_, "panel");
        }

        public Layout getLayout() { return layout; }
        public float getPadding() { return padding; }
        public float getSpacing() { return spacing; }
    }

    // ==================== IMAGE BUILDER ====================

    public static class ImageBuilder extends BaseBuilder<ImageBuilder> {
        private String source;
        private ScaleMode scaleMode = ScaleMode.FIT;

        public ImageBuilder source(String path) {
            this.source = path;
            return this;
        }

        public ImageBuilder scaleMode(ScaleMode mode) {
            this.scaleMode = mode;
            return this;
        }

        public UIComponent build() {
            Object native_ = adapter.createImage(this);
            return new UIComponent(native_, "image");
        }

        public String getSource() { return source; }
        public ScaleMode getScaleMode() { return scaleMode; }
    }

    // ==================== ENUMS ====================

    public enum TextAlign {
        LEFT, CENTER, RIGHT
    }

    public enum Layout {
        NONE, VERTICAL, HORIZONTAL, GRID
    }

    public enum ScaleMode {
        FIT, FILL, STRETCH, NONE
    }
}
