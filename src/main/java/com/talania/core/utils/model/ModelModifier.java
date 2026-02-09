package com.talania.core.utils.model;

import java.util.*;
import java.util.function.Consumer;

/**
 * Utility for modifying entity models at runtime.
 * 
 * <p>Provides an abstraction layer for:
 * <ul>
 *   <li>Attaching components to models (e.g., elf ears, hats)</li>
 *   <li>Scaling model parts</li>
 *   <li>Visibility toggles</li>
 *   <li>Material/texture swapping</li>
 * </ul>
 * 
 * <p>Usage:
 * <pre>{@code
 * // Attach ears to a player model
 * ModelModifier.attach(playerId, "head", "elf_ears", AttachOptions.defaults());
 * 
 * // Scale a body part
 * ModelModifier.scale(playerId, "arms", 1.2f, 1.0f, 1.2f);
 * 
 * // Hide a model component
 * ModelModifier.setVisible(playerId, "helmet", false);
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class ModelModifier {

    private static final Map<String, List<Attachment>> attachments = new HashMap<>();
    private static Consumer<ModelOperation> operationHandler = op -> {
        System.out.println("[TalaniaCore] Model operation handler not configured: " + op.type);
    };

    private ModelModifier() {}

    // ==================== CONFIGURATION ====================

    /**
     * Set the operation handler for model modifications.
     * This must be set by platform integration code.
     * 
     * @param handler Consumer that processes model operations
     */
    public static void setOperationHandler(Consumer<ModelOperation> handler) {
        operationHandler = handler != null ? handler : op -> {};
    }

    // ==================== ATTACHMENTS ====================

    /**
     * Attach a component to an entity's model.
     * 
     * @param entityId Entity identifier
     * @param boneName Name of the bone/slot to attach to (e.g., "head", "hand_r")
     * @param componentId ID of the component to attach
     * @param options Attachment options
     * @return An Attachment handle for later removal
     */
    public static Attachment attach(String entityId, String boneName, String componentId, AttachOptions options) {
        Attachment attachment = new Attachment(entityId, boneName, componentId, options);
        
        attachments.computeIfAbsent(entityId, k -> new ArrayList<>()).add(attachment);
        
        operationHandler.accept(new ModelOperation(
                OperationType.ATTACH,
                entityId,
                Map.of(
                        "bone", boneName,
                        "component", componentId,
                        "options", options
                )
        ));
        
        return attachment;
    }

    /**
     * Attach with default options.
     */
    public static Attachment attach(String entityId, String boneName, String componentId) {
        return attach(entityId, boneName, componentId, AttachOptions.defaults());
    }

    /**
     * Detach a component from an entity's model.
     * 
     * @param attachment The attachment to remove
     */
    public static void detach(Attachment attachment) {
        if (attachment == null) return;
        
        List<Attachment> entityAttachments = attachments.get(attachment.entityId);
        if (entityAttachments != null) {
            entityAttachments.remove(attachment);
        }
        
        operationHandler.accept(new ModelOperation(
                OperationType.DETACH,
                attachment.entityId,
                Map.of(
                        "bone", attachment.boneName,
                        "component", attachment.componentId
                )
        ));
    }

    /**
     * Detach all components from an entity.
     */
    public static void detachAll(String entityId) {
        List<Attachment> removed = attachments.remove(entityId);
        if (removed != null) {
            for (Attachment attachment : removed) {
                operationHandler.accept(new ModelOperation(
                        OperationType.DETACH,
                        entityId,
                        Map.of("component", attachment.componentId)
                ));
            }
        }
    }

    /**
     * Get all attachments for an entity.
     */
    public static List<Attachment> getAttachments(String entityId) {
        return new ArrayList<>(attachments.getOrDefault(entityId, Collections.emptyList()));
    }

    // ==================== TRANSFORMATIONS ====================

    /**
     * Scale a bone/part of the model.
     * 
     * @param entityId Entity identifier
     * @param boneName Name of the bone to scale
     * @param x X scale factor
     * @param y Y scale factor
     * @param z Z scale factor
     */
    public static void scale(String entityId, String boneName, float x, float y, float z) {
        operationHandler.accept(new ModelOperation(
                OperationType.SCALE,
                entityId,
                Map.of("bone", boneName, "x", x, "y", y, "z", z)
        ));
    }

    /**
     * Scale uniformly.
     */
    public static void scale(String entityId, String boneName, float scale) {
        scale(entityId, boneName, scale, scale, scale);
    }

    /**
     * Reset bone scale to default.
     */
    public static void resetScale(String entityId, String boneName) {
        scale(entityId, boneName, 1.0f, 1.0f, 1.0f);
    }

    // ==================== VISIBILITY ====================

    /**
     * Set visibility of a bone/part.
     * 
     * @param entityId Entity identifier
     * @param boneName Name of the bone
     * @param visible Whether the bone should be visible
     */
    public static void setVisible(String entityId, String boneName, boolean visible) {
        operationHandler.accept(new ModelOperation(
                OperationType.VISIBILITY,
                entityId,
                Map.of("bone", boneName, "visible", visible)
        ));
    }

    // ==================== MATERIALS ====================

    /**
     * Set a material on a model part.
     * 
     * @param entityId Entity identifier
     * @param boneName Name of the bone/mesh
     * @param materialId Material identifier
     */
    public static void setMaterial(String entityId, String boneName, String materialId) {
        operationHandler.accept(new ModelOperation(
                OperationType.MATERIAL,
                entityId,
                Map.of("bone", boneName, "material", materialId)
        ));
    }

    // ==================== INNER CLASSES ====================

    /**
     * Types of model operations.
     */
    public enum OperationType {
        ATTACH,
        DETACH,
        SCALE,
        VISIBILITY,
        MATERIAL
    }

    /**
     * A model operation request.
     */
    public static class ModelOperation {
        public final OperationType type;
        public final String entityId;
        public final Map<String, Object> data;

        public ModelOperation(OperationType type, String entityId, Map<String, Object> data) {
            this.type = type;
            this.entityId = entityId;
            this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        }
    }

    /**
     * Options for attaching components.
     */
    public static class AttachOptions {
        private float offsetX = 0, offsetY = 0, offsetZ = 0;
        private float rotX = 0, rotY = 0, rotZ = 0;
        private float scale = 1.0f;
        private boolean inheritRotation = true;

        public static AttachOptions defaults() {
            return new AttachOptions();
        }

        public AttachOptions offset(float x, float y, float z) {
            this.offsetX = x;
            this.offsetY = y;
            this.offsetZ = z;
            return this;
        }

        public AttachOptions rotation(float x, float y, float z) {
            this.rotX = x;
            this.rotY = y;
            this.rotZ = z;
            return this;
        }

        public AttachOptions scale(float scale) {
            this.scale = scale;
            return this;
        }

        public AttachOptions inheritRotation(boolean inherit) {
            this.inheritRotation = inherit;
            return this;
        }

        public float getOffsetX() { return offsetX; }
        public float getOffsetY() { return offsetY; }
        public float getOffsetZ() { return offsetZ; }
        public float getRotX() { return rotX; }
        public float getRotY() { return rotY; }
        public float getRotZ() { return rotZ; }
        public float getScale() { return scale; }
        public boolean isInheritRotation() { return inheritRotation; }
    }

    /**
     * Handle to an attached component.
     */
    public static class Attachment {
        public final String entityId;
        public final String boneName;
        public final String componentId;
        public final AttachOptions options;

        Attachment(String entityId, String boneName, String componentId, AttachOptions options) {
            this.entityId = entityId;
            this.boneName = boneName;
            this.componentId = componentId;
            this.options = options;
        }

        public void detach() {
            ModelModifier.detach(this);
        }
    }
}
