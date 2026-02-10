package com.talania.core.events;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Simple event bus for inter-module communication.
 * 
 * <p>Provides a publish-subscribe pattern for loose coupling between
 * components. Supports prioritized listeners and event cancellation.
 * 
 * <p>Usage:
 * <pre>{@code
 * // Subscribe to events
 * EventBus.subscribe(PlayerDamageEvent.class, event -> {
 *     if (event.getAmount() > 100) {
 *         event.setCancelled(true);
 *     }
 * });
 * 
 * // Publish events
 * PlayerDamageEvent event = new PlayerDamageEvent(player, 50);
 * EventBus.publish(event);
 * if (!event.isCancelled()) {
 *     // Apply damage
 * }
 * }</pre>
 * 
 * @author TalaniaCore Team
 * @since 0.1.0
 */
public final class EventBus {

    private static final Map<Class<?>, List<ListenerEntry<?>>> listeners = new ConcurrentHashMap<>();

    private EventBus() {}

    // ==================== SUBSCRIBE ====================

    /**
     * Subscribe to events of a specific type.
     * 
     * @param eventType The event class to listen for
     * @param handler The handler to call when the event occurs
     * @param <T> Event type
     * @return A registration that can be used to unsubscribe
     */
    public static <T> EventRegistration subscribe(Class<T> eventType, Consumer<T> handler) {
        return subscribe(eventType, handler, Priority.NORMAL);
    }

    /**
     * Subscribe with a specific priority.
     * Higher priority listeners are called first.
     * 
     * @param eventType The event class to listen for
     * @param handler The handler to call
     * @param priority Execution priority
     * @param <T> Event type
     * @return A registration that can be used to unsubscribe
     */
    public static <T> EventRegistration subscribe(Class<T> eventType, Consumer<T> handler, Priority priority) {
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(handler, "handler cannot be null");

        ListenerEntry<T> entry = new ListenerEntry<>(eventType, handler, priority);
        
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add(entry);
        
        // Sort by priority (higher first)
        sortListeners(eventType);

        return new EventRegistration(eventType, entry);
    }

    // ==================== PUBLISH ====================

    /**
     * Publish an event to all registered listeners.
     * 
     * @param event The event to publish
     * @param <T> Event type
     * @return The event (possibly modified by listeners)
     */
    @SuppressWarnings("unchecked")
    public static <T> T publish(T event) {
        if (event == null) return null;

        Class<?> eventType = event.getClass();
        List<ListenerEntry<?>> eventListeners = listeners.get(eventType);

        if (eventListeners != null) {
            for (ListenerEntry<?> entry : eventListeners) {
                try {
                    ((Consumer<T>) entry.handler).accept(event);
                    
                    // Stop if event is cancelled and cancellable
                    if (event instanceof Cancellable && ((Cancellable) event).isCancelled()) {
                        break;
                    }
                } catch (Exception e) {
                    System.err.println("[TalaniaCore] Event handler error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        return event;
    }

    /**
     * Publish an event and return whether it was cancelled.
     * 
     * @param event A cancellable event
     * @return true if the event was cancelled
     */
    public static boolean publishAndCheckCancelled(Cancellable event) {
        publish(event);
        return event.isCancelled();
    }

    // ==================== UNSUBSCRIBE ====================

    /**
     * Unsubscribe using a registration.
     */
    public static void unsubscribe(EventRegistration registration) {
        if (registration == null) return;
        
        List<ListenerEntry<?>> eventListeners = listeners.get(registration.eventType);
        if (eventListeners != null) {
            eventListeners.remove(registration.entry);
        }
    }

    /**
     * Unsubscribe all listeners for an event type.
     */
    public static void unsubscribeAll(Class<?> eventType) {
        listeners.remove(eventType);
    }

    /**
     * Clear all listeners.
     */
    public static void clear() {
        listeners.clear();
    }

    // ==================== INTERNAL ====================

    private static void sortListeners(Class<?> eventType) {
        List<ListenerEntry<?>> list = listeners.get(eventType);
        if (list != null && list.size() > 1) {
            list.sort(Comparator.comparingInt((ListenerEntry<?> e) -> e.priority.ordinal()).reversed());
        }
    }

    // ==================== INNER CLASSES ====================

    /**
     * Event execution priority.
     */
    public enum Priority {
        LOWEST,
        LOW,
        NORMAL,
        HIGH,
        HIGHEST,
        /** Monitor priority - should not modify events, only observe */
        MONITOR
    }

    /**
     * Interface for cancellable events.
     */
    public interface Cancellable {
        boolean isCancelled();
        void setCancelled(boolean cancelled);
    }

    /**
     * Registration handle for unsubscribing.
     */
    public static class EventRegistration {
        private final Class<?> eventType;
        private final ListenerEntry<?> entry;

        private EventRegistration(Class<?> eventType, ListenerEntry<?> entry) {
            this.eventType = eventType;
            this.entry = entry;
        }

        public void unsubscribe() {
            EventBus.unsubscribe(this);
        }
    }

    private static class ListenerEntry<T> {
        final Class<T> eventType;
        final Consumer<T> handler;
        final Priority priority;

        ListenerEntry(Class<T> eventType, Consumer<T> handler, Priority priority) {
            this.eventType = eventType;
            this.handler = handler;
            this.priority = priority != null ? priority : Priority.NORMAL;
        }
    }
}
