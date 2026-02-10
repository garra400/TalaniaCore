package com.talania.core.utils.animation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Helper utilities for working with entity animations.
 * 
 * <p>Provides a platform-agnostic interface for animation management.
 * Actual implementation depends on the target platform (Hytale API).
 * 
 * <p>Usage:
 * <pre>{@code
 * // Register animation callback
 * AnimationHelper.onAnimationEnd(entityId, "attack_swing", () -> {
 *     System.out.println("Attack finished!");
 * });
 * 
 * // Play animation with options
 * AnimationHelper.play(entity, "idle", AnimationOptions.loop().speed(1.5f));
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class AnimationHelper {

    private static final Map<String, Map<String, Runnable>> animationEndCallbacks = new HashMap<>();
    private static Consumer<AnimationRequest> animationPlayer = request -> {
        // Default no-op, must be set by platform integration
        System.out.println("[TalaniaCore] Animation player not configured: " + request.animationId);
    };

    private AnimationHelper() {}

    // ==================== CONFIGURATION ====================

    /**
     * Set the animation player implementation.
     * This must be called during mod initialization to enable animations.
     * 
     * @param player Consumer that handles animation requests
     */
    public static void setAnimationPlayer(Consumer<AnimationRequest> player) {
        animationPlayer = player != null ? player : request -> {};
    }

    // ==================== PLAYBACK ====================

    /**
     * Play an animation on an entity.
     * 
     * @param entityId Entity identifier (platform-specific)
     * @param animationId The animation to play
     */
    public static void play(Object entityId, String animationId) {
        play(entityId, animationId, AnimationOptions.defaults());
    }

    /**
     * Play an animation with options.
     * 
     * @param entityId Entity identifier
     * @param animationId The animation to play
     * @param options Playback options
     */
    public static void play(Object entityId, String animationId, AnimationOptions options) {
        AnimationRequest request = new AnimationRequest(entityId, animationId, options);
        animationPlayer.accept(request);
    }

    /**
     * Stop an animation on an entity.
     */
    public static void stop(Object entityId, String animationId) {
        play(entityId, animationId, AnimationOptions.defaults().stop(true));
    }

    /**
     * Stop all animations on an entity.
     */
    public static void stopAll(Object entityId) {
        play(entityId, "*", AnimationOptions.defaults().stop(true));
    }

    // ==================== CALLBACKS ====================

    /**
     * Register a callback for when an animation ends.
     * 
     * @param entityId Entity identifier (as string)
     * @param animationId Animation identifier
     * @param callback Called when the animation completes
     */
    public static void onAnimationEnd(String entityId, String animationId, Runnable callback) {
        animationEndCallbacks
                .computeIfAbsent(entityId, k -> new HashMap<>())
                .put(animationId, callback);
    }

    /**
     * Notify that an animation has ended (called by platform integration).
     */
    public static void notifyAnimationEnd(String entityId, String animationId) {
        Map<String, Runnable> entityCallbacks = animationEndCallbacks.get(entityId);
        if (entityCallbacks != null) {
            Runnable callback = entityCallbacks.remove(animationId);
            if (callback != null) {
                try {
                    callback.run();
                } catch (Exception e) {
                    System.err.println("[TalaniaCore] Animation callback error: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Clear all animation callbacks for an entity.
     */
    public static void clearCallbacks(String entityId) {
        animationEndCallbacks.remove(entityId);
    }

    // ==================== INNER CLASSES ====================

    /**
     * Options for animation playback.
     */
    public static class AnimationOptions {
        private boolean loop = false;
        private float speed = 1.0f;
        private float blendTime = 0.1f;
        private int priority = 0;
        private boolean stop = false;

        public static AnimationOptions defaults() {
            return new AnimationOptions();
        }

        public static AnimationOptions loop() {
            return new AnimationOptions().setLoop(true);
        }

        public static AnimationOptions once() {
            return new AnimationOptions().setLoop(false);
        }

        public AnimationOptions setLoop(boolean loop) {
            this.loop = loop;
            return this;
        }

        public AnimationOptions speed(float speed) {
            this.speed = speed;
            return this;
        }

        public AnimationOptions blendTime(float seconds) {
            this.blendTime = seconds;
            return this;
        }

        public AnimationOptions priority(int priority) {
            this.priority = priority;
            return this;
        }

        public AnimationOptions stop(boolean stop) {
            this.stop = stop;
            return this;
        }

        public boolean isLoop() { return loop; }
        public float getSpeed() { return speed; }
        public float getBlendTime() { return blendTime; }
        public int getPriority() { return priority; }
        public boolean isStop() { return stop; }
    }

    /**
     * Request to play an animation.
     */
    public static class AnimationRequest {
        public final Object entityId;
        public final String animationId;
        public final AnimationOptions options;

        public AnimationRequest(Object entityId, String animationId, AnimationOptions options) {
            this.entityId = entityId;
            this.animationId = animationId;
            this.options = options != null ? options : AnimationOptions.defaults();
        }
    }
}
