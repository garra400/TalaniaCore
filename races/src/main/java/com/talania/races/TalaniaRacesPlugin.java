package com.talania.races;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.talania.races.api.TalaniaApiImpl;

import javax.annotation.Nonnull;

/**
 * Races module plugin. Registers the Talania API implementation for races.
 */
public final class TalaniaRacesPlugin extends JavaPlugin {
    private final RaceService raceService = new RaceService();

    public TalaniaRacesPlugin(@Nonnull JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        // API implementation is available via the plugin instance.
    }

    public RaceService raceService() {
        return raceService;
    }
}
