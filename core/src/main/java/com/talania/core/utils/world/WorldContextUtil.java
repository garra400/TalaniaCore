package com.talania.core.utils.world;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Shared world context utilities (time, environment, terrain).
 */
public final class WorldContextUtil {
    /**
     * Cached value for nighttime seconds to avoid reflective lookup on the hot path.
     */
    private static final double NIGHT_SECONDS = initNightSeconds();

    private WorldContextUtil() {
    }

    /**
     * Check if it is currently daytime in the given world store.
     */
    public static boolean isDay(Store<EntityStore> store) {
        WorldTimeResource time = timeResource(store);
        if (time == null) {
            return true;
        }
        double daySeconds = WorldTimeResource.DAYTIME_SECONDS;
        double nightSeconds = resolveNightSeconds(daySeconds);
        double totalSeconds = daySeconds + nightSeconds;
        if (totalSeconds <= 0) {
            return true;
        }
        double dayStart = WorldTimeResource.SUNRISE_SECONDS / totalSeconds;
        double dayEnd = dayStart + (daySeconds / totalSeconds);
        double progress = time.getDayProgress();
        if (dayEnd >= 1.0) {
            return progress >= dayStart || progress <= (dayEnd - 1.0);
        }
        return progress >= dayStart && progress <= dayEnd;
    }

    /**
     * Check if it is currently nighttime in the given world store.
     */
    public static boolean isNight(Store<EntityStore> store) {
        return !isDay(store);
    }

    /**
     * Get the current day progress (0..1) if available.
     */
    public static float dayProgress(Store<EntityStore> store) {
        WorldTimeResource time = timeResource(store);
        return time == null ? 0.0f : time.getDayProgress();
    }

    private static WorldTimeResource timeResource(Store<EntityStore> store) {
        if (store == null) {
            return null;
        }
        return (WorldTimeResource) store.getResource(WorldTimeResource.getResourceType());
    }

    private static double initNightSeconds() {
        try {
            return WorldTimeResource.class.getField("NIGHTTIME_SECONDS").getDouble(null);
        } catch (ReflectiveOperationException ignored) {
            return WorldTimeResource.DAYTIME_SECONDS * (2.0 / 3.0);
        }
    }

    private static double resolveNightSeconds(double daySeconds) {
        return NIGHT_SECONDS;
    }
}
