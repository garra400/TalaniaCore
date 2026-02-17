package com.talania.core.utils.world;

import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.modules.time.WorldTimeResource;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;

/**
 * Shared world context utilities (time, environment, terrain).
 */
public final class WorldContextUtil {
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
        double start = WorldTimeResource.SUNRISE_SECONDS;
        double end = start + WorldTimeResource.DAYTIME_SECONDS;
        return time.isDayTimeWithinRange(start, end);
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
}
